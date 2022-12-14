package com.ssafy.colors.service;

import com.ssafy.colors.database.entity.*;
import com.ssafy.colors.database.repository.*;
import com.ssafy.colors.enumdata.RoomStatus;
import com.ssafy.colors.enumdata.RoomType;
import com.ssafy.colors.request.Colorset;
import com.ssafy.colors.request.ResultReq;
import com.ssafy.colors.request.RoomReq;
import com.ssafy.colors.response.ResultRes;
import com.ssafy.colors.response.RoomRes;
import com.ssafy.colors.util.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    MeetingResultRepository meetingResultRepository;

    @Autowired
    MeetingVoteRepository meetingVoteRepository;

    @Autowired
    private RandomStringGenerator randomStringGenerator;

    @Override
    public RoomRes saveMeetingRoomInfo(RoomReq roomReq) {
        System.out.println("Enter RoomService - saveMeetingRoomInfo()");

        Member host = memberRepository.findById(roomReq.getHostid()).get();
        Topic selectedTopic = null;

        RoomType roomType = null;
        if ("single".equals(roomReq.getRoomtype())) {
            roomType = RoomType.SINGLE;
        } else if ("group".equals(roomReq.getRoomtype())) {
            roomType = RoomType.GROUP;
        } else if ("random".equals(roomReq.getRoomtype())) {
            roomType = RoomType.RANDOM;
            selectedTopic = topicRepository.findById(roomReq.getTopicid()).get();
        } else {
            System.out.println("잘못된 파라미터입니다.");
            return null;
        }

        // 랜덤으로 생성된 방 코드가 중복될 경우 최대 5회까지 시도
        String randomCode = null;
        boolean issued = false;

        for (int i = 0; i < 5; i++) {
            randomCode = randomStringGenerator.generateRandomPassword(8).toUpperCase(Locale.ROOT);
            Room tempRoom = roomRepository.findFirstByRoomCodeAndStatus(randomCode, RoomStatus.WAITED);

            if (tempRoom == null) {
                issued = true;
                break;
            } else continue;
        }

        System.out.println("RANDOM CODE : " + randomCode);
        System.out.println("ISSUED : " + issued);

        // 방 코드를 정상 발급 받은 경우
        if (issued) {
            Room room = Room.builder()
                    .host(host)
                    .topic(selectedTopic)
                    .roomCode(randomCode)
                    .roomType(roomType)
                    .cDate(LocalDateTime.now())
                    .status(RoomStatus.WAITED)
                    .build();
            try {
                roomRepository.save(room);

                RoomRes result = RoomRes.builder()
                        .roomid(room.getId())
                        .hostid(room.getHost().getId())
                        .roomcode(room.getRoomCode())
                        .title(selectedTopic == null ? null : room.getTopic().getTitle())
                        .roomtype(room.getRoomType().toString().toLowerCase())
                        .status(room.getStatus().toString().toLowerCase())
                        .build();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // 정상적으로 발급받지 못한 경우(5회 중복)
        else {
            return null;
        }
    }

    @Override
    public Room checkRoomCode(String code) {
        Room room = roomRepository.findFirstByRoomCodeAndStatus(code, RoomStatus.WAITED);

        if (room != null) return room;
        else return null;
    }

    @Override
    public Room findRandomRoom(long topicId) {
        Optional<Topic> topic = topicRepository.findById(topicId);
        Room output = null;

        if(topic.isPresent()) {
            RoomStatus status = RoomStatus.WAITED;
            List<Room> rooms = roomRepository.findAccessibleRoom(topic.get(), status, false);

            if(!rooms.isEmpty()) {
                output = rooms.get(0);
            }
        }

        return output;
    }

    @Override
    public boolean changeRoomCapacityStatus(long roomId) {
        int result = roomRepository.reverseCapacity(roomId);
        return result > 0;
    }

    @Override
    public boolean changeRoomStatus(long hostId, long roomId) {
        Optional<Member> host = memberRepository.findById(hostId);

        if (host.isPresent()) {
            Room room = roomRepository.findByIdAndHost(roomId, host.get());
            if (room == null) return false;

            int result = 0;

            if (RoomStatus.WAITED.equals(room.getStatus())) {
                result = roomRepository.updateRoomStatus(roomId, host.get(), RoomStatus.STARTED);
            } else if (RoomStatus.STARTED.equals(room.getStatus())) {
                result = roomRepository.updateRoomStatus(roomId, host.get(), RoomStatus.CLOSED);
            }

            return result > 0;
        } else {
            return false;
        }

    }

    @Override
    public boolean saveMeetingResult(ResultReq results) {
        try {
            Long roomId = results.getRoomid();
            Long userId = results.getUserid();
            Room room = roomRepository.findById(roomId).get();
            Member member = memberRepository.findById(userId).get();
            List<Colorset> colorsets = results.getColorset();

            for (int i = 0; i < colorsets.size(); i++) {
                MeetingResult meetingResult = MeetingResult.builder()
                        .room(room)
                        .member(member)
                        .imageUrl(colorsets.get(i).getUrl())
                        .colorCode(colorsets.get(i).getCode())
                        .build();
                meetingResultRepository.save(meetingResult);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ResultRes> getMeetingResult(long roomId) {
        Room room = roomRepository.findById(roomId).get();

        List<Member> memberList = meetingResultRepository.findDistinctMember(room);
        for(Member m : memberList){
            System.out.println("member = " + m);
        }
        List<MeetingResult> resultList = meetingResultRepository.findByRoomOrderByMember(room);
        for(MeetingResult m : resultList){
            System.out.println("MeetingResult = " + m);
        }
        if (!resultList.isEmpty()) {
            List<ResultRes> result = new ArrayList<>();

            for(int i =0 ; i<memberList.size(); i++){
                Member member = memberList.get(i);
                result.add(
                        new ResultRes(member.getId(), member.getUserId(),member.getName(),member.getNickname(),
                                new ArrayList<String>(),new ArrayList<String>()));
            }
            for(int i = 0 ; i<resultList.size(); i++){
                MeetingResult cur = resultList.get(i);
                for(int j =0 ;j<result.size(); j++){
                    if(cur.getMember().getUserId().equals(result.get(j).getUserid())){
                        result.get(j).getUrls().add(cur.getImageUrl());
                        result.get(j).getColors().add(cur.getColorCode());
                        break;
                    }
                }
            }
            for(ResultRes r :result){
                System.out.println("ResultRes = " + r);
            }
            System.out.println("result = " + result);

            return result;
        } else {
            return null;
        }
    }

    @Override
    public boolean saveTournamentResult(Map<String, Object> params) {
        Long roomId = Long.parseLong(params.get("roomid").toString());
        Long userId = Long.parseLong(params.get("userid").toString());
        String colorCode = (String) params.get("code");

        Room room = roomRepository.findById(roomId).get();
        Member member = memberRepository.findById(userId).get();

        int result = meetingResultRepository.updateTop1(room, member, colorCode);

        return result > 0;
    }

    @Override
    public boolean saveVoteResult(Map<String, Object> params) {
        Long roomId = Long.parseLong(params.get("roomid").toString());
        Long voterId = Long.parseLong(params.get("voterid").toString());
        List<Map<String, Object>> content = (List<Map<String, Object>>) params.get("content");
        try {
            Room room = roomRepository.findById(roomId).get();
            Member voter = memberRepository.findById(voterId).get();

            for (int i = 0; i < content.size(); i++) {
                Long targetId = Long.parseLong(content.get(i).get("targetid").toString());
                String code = (String) content.get(i).get("code");

                Member member = memberRepository.findById(targetId).get();

                MeetingResult meetingResult = meetingResultRepository.findByRoomAndMemberAndColorCode(room, member, code);

                meetingVoteRepository.save(MeetingVote.builder()
                        .meetingResult(meetingResult)
                        .member(voter)
                        .build());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean addVote(long roomId) {
        try {
            Room room = roomRepository.findById(roomId).get();
            List<MeetingResult> meetingResultList = meetingResultRepository.findByRoomOrderByMember(room);

            // 참가자의 고유 아이디를 한 번만 추출함
            Set<Long> distinctParticipants = new HashSet<>();
            for (MeetingResult mr : meetingResultList) {
                distinctParticipants.add(mr.getMember().getId());
            }

            Object[] setArr = distinctParticipants.toArray();

            for (int i = 0; i < distinctParticipants.size(); i++) {
                Map<Long, Integer> sum = new HashMap<>();

                for (int j = 0; j < meetingResultList.size(); j++) {
                    if ((setArr[i] == meetingResultList.get(j).getMember().getId())) {
                        MeetingResult cur = meetingResultList.get(j);
                        List<MeetingVote> meetingVoteList = meetingVoteRepository.findByMeetingResult(cur);

                        for (int k = 0; k < meetingVoteList.size(); k++) {
                            Long key = meetingVoteList.get(k).getMeetingResult().getId();
                            if (!sum.containsKey(key)) {
                                sum.put(key, 1);
                            } else {
                                sum.put(key, sum.get(key) + 1);
                            }
                        }
                    }
                }

                // 중복 Value가 존재할 때, 최대값인 Key를 추출
                Long maxKey = sum.entrySet().stream().max((m1, m2) -> m1.getValue() > m2.getValue() ? 1 : -1).get().getKey();
                meetingResultRepository.updateTop1ById(maxKey);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getVoteResult(Map<String, Object> params) {
        Long roomId = Long.parseLong(params.get("roomid").toString());
        Long userId = Long.parseLong(params.get("userid").toString());

        Room room = roomRepository.findById(roomId).get();
        Member member = memberRepository.findById(userId).get();

        Map<String, Object> output = new HashMap<>();

        if (RoomType.SINGLE.equals(room.getRoomType())) {
            MeetingResult meetingResult = meetingResultRepository.findByRoomAndMemberAndTop1(room, member, true).get(0);

            output.put("name", member.getName());
            output.put("url", meetingResult.getImageUrl());
            output.put("code", meetingResult.getColorCode());

            return output;
        } else {
            List<MeetingResult> meetingResultList = meetingResultRepository.findByRoomAndMember(room, member);
            List<Map<String, Object>> unit = new ArrayList<>();

            for (int i = 0; i < meetingResultList.size(); i++) {
                MeetingResult meetingResult = meetingResultList.get(i);
                List<MeetingVote> meetingVotesList = meetingVoteRepository.findByMeetingResult(meetingResult);

                if (RoomType.GROUP.equals(room.getRoomType())) {
                    for (int j = 0; j < meetingVotesList.size(); j++) {
                        Member voter = memberRepository.findById(meetingVotesList.get(j).getMember().getId()).get();
                        MeetingResult pick = meetingResultRepository.findById(meetingVotesList.get(j).getMeetingResult().getId()).get();

                        Map<String, Object> attr = new HashMap<>();
                        attr.put("voter", voter.getName());
                        attr.put("url", pick.getImageUrl());
                        attr.put("code", pick.getColorCode());
                        unit.add(attr);
                    }
                } else if (RoomType.RANDOM.equals(room.getRoomType())) {
                    for (int j = 0; j < meetingVotesList.size(); j++) {
                        Member voter = memberRepository.findById(meetingVotesList.get(j).getMember().getId()).get();
                        MeetingResult pick = meetingResultRepository.findById(meetingVotesList.get(j).getMeetingResult().getId()).get();

                        Map<String, Object> attr = new HashMap<>();
                        attr.put("voter", voter.getNickname());
                        attr.put("url", pick.getImageUrl());
                        attr.put("code", pick.getColorCode());
                        unit.add(attr);
                    }
                }
            }
            return unit;
        }
    }

    @Override
    public Colorset getTop1ColorInfo(Map<String, Object> params) {
        Long roomId = Long.parseLong(params.get("roomid").toString());
        Long userId = Long.parseLong(params.get("userid").toString());

        Room room = roomRepository.findById(roomId).get();
        Member member = memberRepository.findById(userId).get();
//        MeetingResult meetingResult = meetingResultRepository.findByRoomAndMemberAndTop1(room, member, true);
        MeetingResult meetingResult = meetingResultRepository.findByRoomAndMemberAndTop1(room, member, true).get(0);

        if (meetingResult != null) {
            return Colorset.builder()
                    .url(meetingResult.getImageUrl())
                    .code(meetingResult.getColorCode())
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getMyPageColorInfo(long userId) {
        boolean single = false;
        boolean group = false;
        boolean random = false;

        try {
//          List<Room> roomList = roomRepository.getRecentMeetingInfo(userId);
            List<Room> roomList = roomRepository.getMeetingInfo(userId);
            Member member = memberRepository.findById(userId).get();
            List<Map<String, Object>> unitArr = new ArrayList<>();
            System.out.println("roomList = " + roomList);
            for (int i = roomList.size() - 1; i > 0; i--) {
                if (single && group && random)
                    break;
                Room room = roomList.get(i);
                MeetingResult top1 = null;
                RoomType roomType = room.getRoomType();
                if ((roomType == RoomType.GROUP && group) || (roomType == RoomType.RANDOM && random) || (roomType == RoomType.SINGLE && single))
                    continue;
                List<MeetingResult> meetingResultList = meetingResultRepository.findByRoomAndMember(room, member);
                if (meetingResultList.isEmpty()) {
                    continue;
                }
                List<String> urls = new ArrayList<>();
                List<String> colorCodes = new ArrayList<>();

                for (int j = 0; j < meetingResultList.size(); j++) {
                    MeetingResult meetingResult = meetingResultList.get(j);
                    urls.add(meetingResult.getImageUrl());
                    colorCodes.add(meetingResult.getColorCode());
                    if (meetingResult.isTop1()) top1 = meetingResult;
                }
                if (top1 == null) {
                    continue;
                }
                if (roomType == RoomType.GROUP) {
                    group = true;
                } else if (roomType == RoomType.SINGLE) {
                    single = true;
                } else {
                    random = true;
                }
                Map<String, Object> unit = new HashMap<>();
                unit.put("roomtype", roomType.toString().toLowerCase());
                unit.put("url", urls);
                unit.put("code", colorCodes);
                unit.put("top1", new Colorset(top1.getImageUrl(), top1.getColorCode()));
                if (RoomType.RANDOM.equals(roomType)) {
                    unit.put("title", room.getTopic().getTitle());
                }
                unitArr.add(unit);
            }
            return unitArr;
        } catch (Exception e) {
            return null;
        }
    }

}
