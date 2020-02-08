package com.atbm.gmall.pms.service.impl;

import com.atbm.gmall.pms.entity.Comment;
import com.atbm.gmall.pms.mapper.CommentMapper;
import com.atbm.gmall.pms.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品评价表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}
