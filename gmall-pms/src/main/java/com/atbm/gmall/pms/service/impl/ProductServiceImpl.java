package com.atbm.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.constant.EsConstant;
import com.atbm.gmall.pms.entity.*;
import com.atbm.gmall.pms.mapper.*;
import com.atbm.gmall.pms.service.ProductService;
import com.atbm.gmall.to.es.EsProduct;
import com.atbm.gmall.to.es.EsProductAttributeValue;
import com.atbm.gmall.to.es.EsSkuproductIofn;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.product.PmsProductParam;
import com.atbm.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Slf4j
@Service
@Component
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {


    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;

    @Autowired
    ProductFullReductionMapper productFullReductionMapper;

    @Autowired
    ProductLadderMapper productLadderMapper;

    @Autowired
    SkuStockMapper skuStockMapper;

    @Autowired
    MemberPriceMapper memberPriceMapper;

    @Autowired
    JestClient jestClient;
/*
* 当前线程共享变量同样的数据
*   因为在spring中是单例存在的
*       -->直接声明变量会导致 同时读写 问题
* */
    //线程共享数据类
    ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    //key是当前线程不会出错
    private Map<Thread,Long> map = new HashMap<>();


    @Override
    public PageInfoVo productPageInfo(PmsProductQueryParam param) {


        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if (param.getBrandId()!=null){
            //前台传值品牌ID
            wrapper.eq("brand_id",param.getBrandId());
        }
        if (!StringUtils.isEmpty(param.getKeyword())){
            //前台传值模糊商品名
            wrapper.like("name",param.getKeyword());
        }
        if (param.getProductCategoryId()!=null){
            //前台传值商品分类ID
            wrapper.eq("product_category_id",param.getProductCategoryId());
        }
        if (!StringUtils.isEmpty(param.getProductSn())){
            //前台传值商品货号
            wrapper.like("product_sn",param.getProductSn());
        }
        if (param.getPublishStatus()!=null){
            //前台传值商品状态
            wrapper.eq("publish_status",param.getPublishStatus());
        }
        if (param.getVerifyStatus()!=null){
            //前台传值商品审核状态
            wrapper.eq("verify_status",param.getVerifyStatus());
        }

        IPage<Product> page = productMapper.selectPage(
                new Page<Product>(param.getPageNum(), param.getPageSize()), wrapper);
        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(),page.getPages(),param.getPageSize(),
                page.getRecords(),page.getCurrent());
        System.out.println("OK+++++++++++++++service+++++++++++++++++++++"+pageInfoVo);
        return pageInfoVo;
    }
/*
*
* 商品信息的保存操作数张表
*
*  事务:传播行为
*       （1）商品核心信息出错才回滚(基本数据,sku)报错才回滚
*
*       （2）事务传播行为;propagation：当前方法的事务如何传播下去(内部方法如果用事务,是否和他共用一个事务)
*               1、REQUIRED:默认传播级别(必须共同使用一个事务)如果没有就创建一个事务
*               2、REQUIRES_NEW:（总是用新的事物即使存在也会创建新的）只要执行过再出错也不会回滚
*               3、SUPPORTS(支持):有事务就用,没事务就不用
*               4、MANDATORY(强制):一定要有事务没事务就报错
*               5、NOT_SUPPORTED(不支持):不支持事务,有事务就挂起
*               6、NEVER(从不使用):不支持事务,有事务就抛异常
*               7、NESTED:开启一个子事务(mysql不支持)
*        （3）事务问题
*               service自己调用自己的方法，无法添加上真正自己内部调整的每个独立的事务:
*                  (jvm内部会把同类间的调用复制粘贴到调用方法内不经过事务)
*               解决：如果是对象.方法()就OK
*                   1、普通方法导入jdbc-starter,开启事务功能@EnableTransactionManagement,加上@Transactional
*                   2.方法自己调自己类里面加不上事务
*                       1）、导入aop包，开启代理对象相关功能
*                       2）、获取到当前类真正的代理对象，去调方法
*                           （1）、@EnableAspectJAutoProxy(exposeProxy = true):暴漏代理对象
*                           （2）、获取 ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();
*                               当前类的代理对象 proxy.方法();
*
* ================================================================================
*
* 事务:隔离级别
*        解决读写加锁问题(数据库底层方案) mysql默认(可重复读(快照))
*     读未提交:写入时不加锁
*     读以提交:写入时加锁
*     可重复读:读的时候也加锁
*     串行化:
*
* =================================================================================
*
* 异常回滚策略 rollbackFor
*       1、运行时异常(不受检查异常)-->一定回滚
*           数学运算异常
*       2、编译时异常(受检查异常)-->默认不回滚
*           FileNotFound(文件没找到);必须抛出(throws)或者try-catch
*           rollbackFor指定那些异常一定回滚
* */
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = {FileNotFoundException.class},
            noRollbackFor = {ArithmeticException.class})
    @Override
    public void saveproduct(PmsProductParam productParam) {
        /*
        * （1）|（5）不轻易回滚
        * */
        //当前方法的代理对象
        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();

        //（1）pms_product基本信息表
        proxy.saveBaseInfo(productParam);

        //（5）pms_sku_stock  sku库存表
        proxy.saveSkuStock(productParam);

        //以下都可以try-catch互不影响

        //（2）pms_product_attribute_value商品属性值
        proxy.saveProductAttributeValue(productParam);

        //（3）pms_product_full_reduction商品满减信息
        proxy.saveProductFullReduction(productParam);

        //（4）pms_product_ladder满减表
        proxy.saveProductLadder(productParam);
    }
    /*
    *
    * 对数据库插入数据
    * dubbo远程调用经常超时,dubbo会默认重试导致方法被多次调用,可能导致产生多个同样的数据
    * cudService增删改 rService读
    * 操作数据库数据的最好用dubbo快速失败模式
    *
    * 改掉默认的mapping信息
    *   1.改掉部分次的字段
    *
    *  */
    @Override
    public void  updatePublishStatus(List<Long> ids, Integer publishStatus) {
        if (publishStatus == 0){
            //下架---->改数据库状态->删ES
            ids.forEach((id)->{
                //改数据库状态
                setProductOPublishStatus(publishStatus, id);
                //删Es
                deleteProductFromEs(id);
            });
        }else{
            //上架---->改数据库状态->添ES
            //1、对于数据库只是修改商品状态
            ids.forEach((id)->{
                //改数据库状态
                setProductOPublishStatus(publishStatus, id);
                //加Es
                saveProductToEs(id);
            });
        }
    }
    /*
    * 按照商品ID查出商品
    * */
    @Override
    public EsProduct productAllInfo(Long id) {
        EsProduct esProduct = null;
        //生成ES使用的操作语句(dsl)query->term->id
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id",id));
        //构建builder语句进行在->PRODUCT_ES_INDEX索引下的->PRODUCT_INFO_ES_TYPE类型->操作
        Search build = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_INFO_ES_TYPE)
                .build();
        try {
            //得出结果
            SearchResult execute = jestClient.execute(build);
            //execute.getHits得到hits属性下的值封装到EsProduct
            List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
            esProduct = hits.get(0).source;
        } catch (IOException e) {

        }
        return esProduct;
    }
    /*
     * 按照skuID查出商品
     * */
    @Override
    public EsProduct productSkuInFo(Long id) {
        EsProduct esProduct = null;
        //生成ES使用的操作语句(dsl)query->nestedQuery(嵌套内部的查询)->skuproductIofns(被嵌套的属性)
            //->termsQuery("skuproductIofns属性下的id属性",值)->null(加分项)
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.nestedQuery("skuproductIofns",
                QueryBuilders.termQuery("skuproductIofns.id",id), ScoreMode.None));
        //构建builder语句进行在->PRODUCT_ES_INDEX索引下的->PRODUCT_INFO_ES_TYPE类型->操作
        Search build = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_INFO_ES_TYPE)
                .build();
        try {
            //得出结果
            SearchResult execute = jestClient.execute(build);
            //execute.getHits得到hits属性下的值封装到EsProduct
            List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
            esProduct = hits.get(0).source;
        } catch (IOException e) {

        }
        return esProduct;
    }
    /*
    * 删除/批量删除->商品
    * */
    @Override
    public void deleteProdect(List<Long> ids, Integer deleteStatus) {
        ids.forEach((id)->{
            productMapper.deleteById(id);
        });
    }

    /*
     * 新品/批量新品->商品
     * */
    @Override
    public void updateNewStatus(List<Long> ids, Integer newStatus) {
        ids.forEach((id)->{
            Product product = new Product();
            product.setId(id);
            product.setNewStatus(newStatus);
            productMapper.updateById(product);
        });
    }
    /*
     * 推荐/批量推荐->商品
     * */
    @Override
    public void updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        ids.forEach((id)->{
            Product product = new Product();
            product.setId(id);
            product.setRecommandStatus(recommendStatus);
            productMapper.updateById(product);
        });
    }
    /*
    * 商品回显
    * */
    @Override
    public Product getUpdateInfo(Long id) {
        Product product = productMapper.selectById(id);
        return product;
    }
    /*
    * 商品修改
    * */
    @Override
    public void update(Long id, PmsProductParam productParam) {

        //商品基本信息设置
        Product product = new Product();
        BeanUtils.copyProperties(productParam,product);
        productMapper.updateById(product);

        //商品阶梯价格设置
        ProductLadder productLadder = new ProductLadder();
        BeanUtils.copyProperties(productParam,productLadder);
        productLadderMapper.updateById(productLadder);

        //商品阶梯价格设置
        ProductFullReduction productFullReduction = new ProductFullReduction();
        BeanUtils.copyProperties(productParam,productFullReduction);
        productFullReductionMapper.updateById(productFullReduction);

        //商品会员价格设置
        MemberPrice memberPrice = new MemberPrice();
        BeanUtils.copyProperties(productParam,memberPrice);
        memberPriceMapper.updateById(memberPrice);

        //商品的sku库存设置
        SkuStock skuStock = new SkuStock();
        BeanUtils.copyProperties(productParam,skuStock);
        skuStockMapper.updateById(skuStock);

        //商品的sku库存设置
        ProductAttributeValue productAttributeValue = new ProductAttributeValue();
        BeanUtils.copyProperties(productParam,productAttributeValue);
        productAttributeValueMapper.updateById(productAttributeValue);
    }
    //商品详情
    @Override
    public Product productInfo2(Long id) {
        Product product = productMapper.selectById(id);
        return product;
    }

    @Override
    public SkuStock skuInfoById(Long skuId) {
        return skuStockMapper.selectById(skuId);
    }

    /*
     * 抽取方法下架Es
     * */
    private void deleteProductFromEs(Long id) {
        Delete delete = new Delete.Builder(id.toString())
                .index(EsConstant.PRODUCT_ES_INDEX)
                .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                .build();
        try {
            DocumentResult execute = jestClient.execute(delete);
            boolean succeeded = execute.isSucceeded();
            if (succeeded){
                log.info("商品:ID为{}ES下架成功",id);
            }else {
                log.error("商品:ID为{}ES下架失败",id);
                //deleteProductFromEs(id);
            }

        }catch (Exception e){
            log.error("商品:ID为{}ES下架失败",id);
            //deleteProductFromEs(id);
        }


    }

    /*
    * 抽取方法上架Es
    * */
    private void saveProductToEs(Long id) {
        //查出商品基本信息
        Product productInfo = productInfo(id);

        //3、ES对象信息
        EsProduct esProduct = new EsProduct();

        //(1)、复制基本属性信息
        //拷贝productInfo中的数据到esProduct内(相同)
        BeanUtils.copyProperties(productInfo,esProduct);

        //2、对于ES要保存商品信息，还要查出这个商品的sku,给es中保存
        //(2)、复制sku属性信息
        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuproductIofn> esSkuproductIofns = new ArrayList<>(stocks.size());

        /*
         * 查出当前商品的sku属性
         * SELECT pa.* FROM pms_product_attribute pa
         * WHERE pa.product_attribute_category_id =
         * (
         *   select pa.product_attribute_category_id
         *   FROM  pms_product_attribute_value pav
         *   		LEFT JOIN pms_product_attribute pa
         *   		on pa.id=pav.product_attribute_id
         *   where pav.product_id=23 AND pa.type=0 LIMIT 1
         * )and type=0 ORDER BY pa.sort DESC
         * */
        List<ProductAttribute> skuAttributeNames = productAttributeValueMapper.selectProdectSaleAttrName(id);
        stocks.forEach((skuStock)->{
            EsSkuproductIofn info = new EsSkuproductIofn();
            //sku基本属性
            BeanUtils.copyProperties(skuStock,info);

            String subTitle = esProduct.getName();
            if(!StringUtils.isEmpty(skuStock.getSp1())){
                subTitle+=" "+skuStock.getSp1();
            }
            if(!StringUtils.isEmpty(skuStock.getSp2())){
                subTitle+=" "+skuStock.getSp2();
            }
            if(!StringUtils.isEmpty(skuStock.getSp3())){
                subTitle+=" "+skuStock.getSp3();
            }
            //sku的特色标题: 小米80 黑色 5G
            info.setSkuTitle(subTitle);

            List<EsProductAttributeValue> skuAttributeValues = new ArrayList<>();

            for (int i = 0; i < skuAttributeNames.size() ; i++) {
                EsProductAttributeValue value = new EsProductAttributeValue();

                value.setName(skuAttributeNames.get(i).getName());
                value.setProductId(id);
                value.setProductAttributeId(skuAttributeNames.get(i).getId());
                value.setType(skuAttributeNames.get(i).getType());
                //value.setValue();值不是固定的 让es去统计

                if (i==0){
                    value.setValue(skuStock.getSp1());
                }
                if(i==1){
                    value.setValue(skuStock.getSp2());
                }
                if(i==2){
                    value.setValue(skuStock.getSp3());
                }
                skuAttributeValues.add(value);
            }

            info.setAttributeValues(skuAttributeValues);
            //sku有多个销售属性;颜色尺码
            esSkuproductIofns.add(info);

        });
        //查sku对应的销售属性(spu)的值
        esProduct.setSkuproductIofns(esSkuproductIofns);
        //(3)、复制spu(公共)属性信息
        /*
         * select pav.*,pa.`name`,pa.type FROM  pms_product_attribute_value pav
         * LEFT JOIN pms_product_attribute pa
         * on pa.id=pav.product_attribute_id
         * where pav.product_id={} AND pa.type=1
         * */
        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProdectBaseAttrAndValue(id);
        esProduct.setAttrValueList(attributeValues);
        try {
            //把商品保存ES
            Index build = new Index.Builder(esProduct)
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                    .id(id.toString())
                    .build();
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if(succeeded){
                log.info("ES中:ID为{}商品上架完成;",id);
            }else{
                log.error("ES中:ID为{}商品保存异常,开始重试;",id);
                //saveProductToEs(id);
            }
        }catch (Exception e){
            log.error("ES中:ID为{}商品数据保存异常;{}",id,e.getMessage());
            //saveProductToEs(id);
        }


    }

    /*
    * 抽取方法改数据库状态
    * */
    public void setProductOPublishStatus(Integer publishStatus, Long id) {
        Product product = new Product();
        //默认所有属性为空
        product.setId(id);
        product.setPublishStatus(publishStatus);
        productMapper.updateById(product);
    }

    @Override
    public Product productInfo(Long id) {
        return productMapper.selectById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBaseInfo(PmsProductParam productParam){
        //（1）pms_product基本信息表
        Product product = new Product();
        //把productParam里的参数拷贝传给product
        BeanUtils.copyProperties(productParam,product);
        productMapper.insert(product);
        threadLocal.set(product.getId());
        map.put(Thread.currentThread(),product.getId());
        //mybatisplus能自动获取刚才的商品ID
        log.debug("刚才的商品ID:{}",product.getId());
        //当前线程号
        log.debug("当前线程...{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(PmsProductParam productParam){
        //（2）pms_product_attribute_value商品属性值
        List<ProductAttributeValue> valueList = productParam.getProductAttributeValueList();
        valueList.forEach((item)->{
            Long aLong = map.get(Thread.currentThread());
            System.out.println("当前线程得到的值。。。Map"+aLong);
            item.setProductId(threadLocal.get());
            int insert = productAttributeValueMapper.insert(item);
        });
        log.debug("当前线程...{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductFullReduction(PmsProductParam productParam){
        //（3）pms_product_full_reduction商品满减信息
        List<ProductFullReduction> fullReductions = productParam.getProductFullReductionList();
        fullReductions.forEach((reduction)->{
            reduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(reduction);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductLadder(PmsProductParam productParam) {
        //（4）pms_product_ladder满减表
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((ladderList)->{
            ladderList.setProductId(threadLocal.get());
            productLadderMapper.insert(ladderList);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSkuStock(PmsProductParam productParam) {
        //（5）pms_sku_stock  sku库存表
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        for (int i = 1; i<=skuStockList.size() ; i++) {
            SkuStock skuStock = skuStockList.get(i-1);
            //页面不传递skucode则自己生成
            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                //skucode则自己生成 1_1 1_2
                //生成规则  商品ID_sku自增ID
                skuStock.setSkuCode(threadLocal.get()+"_"+i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
    }
}
