package com.atbm.gmall.ums.service.impl;

import com.atbm.gmall.ums.entity.Member;
import com.atbm.gmall.ums.mapper.MemberMapper;
import com.atbm.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

}
