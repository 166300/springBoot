package com.atbm.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.pms.entity.Brand;
import com.atbm.gmall.pms.mapper.BrandMapper;
import com.atbm.gmall.pms.service.BrandService;
import com.atbm.gmall.vo.PageInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Slf4j
@Service
@Component
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Autowired
    BrandMapper brandMapper;

    @Override
    public PageInfoVo btandPageInfo(String keyword, Integer pageNum, Integer pageSize) {
        log.info(keyword+"OK+++++++++++++++++++++++++++++++++++");
        QueryWrapper<Brand> name = null;
        if (!StringUtils.isEmpty(keyword)){
            //自动拼 %
            name = new QueryWrapper<Brand>().like("name", keyword);
        }

        IPage<Brand> brandIPage = brandMapper.selectPage(new Page<Brand>(pageNum.longValue(), pageSize.longValue()), name);
        log.info(name+"OK+++++++++++++++++++++++++++++++++++");
        PageInfoVo pageInfoVo=new PageInfoVo(brandIPage.getTotal(),
                brandIPage.getPages(),pageSize.longValue(),brandIPage.getRecords(),
                brandIPage.getCurrent());
        log.info(pageInfoVo+"OK+++++++++++++++++++++++++++++++++++");
        return pageInfoVo;
    }

    @Override
    public void deleteBatch(Long id) {
            brandMapper.delete(new QueryWrapper<Brand>().eq("id",id));

    }

    @Override
    public void updateShowStatus(Long id, Integer showStatus) {
        Brand b=new Brand();
        b.setId(id);
        b.setShowStatus(showStatus);
        brandMapper.updateById(b);
    }

    @Override
    public void updateFactoryStatus(Long id, Integer factoryStatus) {
        Brand b=new Brand();
        b.setId(id);
        b.setFactoryStatus(factoryStatus);
        brandMapper.updateById(b);
    }
}
