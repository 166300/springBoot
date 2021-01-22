package com.atbm.gmall.ums.service;

import com.atbm.gmall.ums.entity.Member;
import com.atbm.gmall.ums.entity.MemberReceiveAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 会员表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface MemberService extends IService<Member> {

    Member login(String username, String password);

    List<MemberReceiveAddress> getMemberAddresses(Long id);

    MemberReceiveAddress getMemberAddressesByAddresseId(Long addressId);
}
