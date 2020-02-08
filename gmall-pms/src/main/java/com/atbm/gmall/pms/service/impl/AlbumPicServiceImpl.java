package com.atbm.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.pms.entity.AlbumPic;
import com.atbm.gmall.pms.mapper.AlbumPicMapper;
import com.atbm.gmall.pms.service.AlbumPicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 画册图片表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
@Component
public class AlbumPicServiceImpl extends ServiceImpl<AlbumPicMapper, AlbumPic> implements AlbumPicService {

}
