package com.ssafy.colors.service;

import com.ssafy.colors.database.entity.Member;
import com.ssafy.colors.database.repository.MemberRepository;
import com.ssafy.colors.request.Mail;
import com.ssafy.colors.request.MemberReq;
import com.ssafy.colors.util.RandomStringGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("memberService")
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RandomStringGenerator randomStringGenerator;

    @Autowired
    MailService mailService;

    @Override
    public boolean checkID(String inputId) {
        return memberRepository.findFirstByUserId(inputId) != null;
    }

    @Override
    public boolean checkNickname(String inputNickname) {
        return memberRepository.findFirstByNickname(inputNickname) != null;
    }

    @Override
    public boolean saveMember(MemberReq memberReq) {
        System.out.println(LocalDateTime.now());
        Member member = Member.builder()
                .userId(memberReq.getUserid())
                .password(memberReq.getPassword())
                .name(memberReq.getName())
                .nickname(memberReq.getNickname())
                .email(memberReq.getEmail())
                .point(0)
                .authGrade(false)
                .regDate(LocalDateTime.now())
                .isDeleted(false)
                .build();

        try {
            memberRepository.save(member);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String findID(MemberReq memberReq) {
        String name = memberReq.getName();
        String email = memberReq.getEmail();
        Member member = memberRepository.findByNameAndEmail(name, email);

        if (member != null) return member.getUserId();
        else return null;
    }

    @Override
    public boolean findPassword(MemberReq memberReq) {
        String userId = memberReq.getUserid();
        String email = memberReq.getEmail();
        String randomPwd = randomStringGenerator.generateRandomPassword(10);
        System.out.println("RAND PWD " + randomPwd);

        int result = memberRepository.updatePassword(randomPwd, userId, email);

        // 임시 비밀번호 변경 성공 시 사용자 메일로 전송
        if (result > 0) {
            Mail mail = Mail.builder()
                    .address(email)
                    .title("[깔맞춤] 임시 비밀번호 발송")
                    .message("임시비밀번호 : " + randomPwd)
                    .build();
            /* 무분별한 메일 전송 방지를 위해 개발 끝나고 테스트 할 때에만 주석 해제할 것 */
            mailService.mailSend(mail);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateMemberInfo(MemberReq memberReq) {
        String inputNickname = memberReq.getNickname();
        String inputName = memberReq.getName();
        String inputId = memberReq.getUserid();

        int result = memberRepository.updateMemberInfo(inputNickname, inputName, inputId);

        return result > 0;
    }

    @Override
    public boolean updatePassword(MemberReq memberReq) {
        String inputPwd = memberReq.getPassword();
        String inputId = memberReq.getUserid();

        int result = memberRepository.changePassword(inputPwd, inputId);

        return result > 0;
    }

    @Override
    public boolean deleteMember(String userId) {
        int result = memberRepository.updateIsDeleted(userId);
        return result > 0;
    }
}