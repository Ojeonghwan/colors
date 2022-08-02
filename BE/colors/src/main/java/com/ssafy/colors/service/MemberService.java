package com.ssafy.colors.service;

import com.ssafy.colors.database.entity.Member;
import com.ssafy.colors.request.MemberReq;
import org.springframework.stereotype.Service;

public interface MemberService {
    public boolean checkID(String inputId);
    public boolean checkNickname(String inputNickname);
    public boolean saveMember(MemberReq memberReq);
    public String findID(MemberReq memberReq);
    public boolean findPassword(MemberReq memberReq);
    public boolean updateMemberInfo(MemberReq memberReq);
    public boolean updatePassword(MemberReq memberReq);
    public boolean deleteMember(String userId);
}