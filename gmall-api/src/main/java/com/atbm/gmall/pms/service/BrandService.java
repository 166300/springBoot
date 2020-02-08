package com.atbm.gmall.pms.service;

import com.atbm.gmall.pms.entity.Brand;
import com.atbm.gmall.vo.PageInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface BrandService extends IService<Brand> {
    PageInfoVo btandPageInfo(String keyword, Integer pageNum, Integer pageSize);
}
