package com.ssafy.colors.database.entity;


import com.ssafy.colors.request.VoteDTO;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Table(name = "topic")
@Entity
@Getter
@Setter
@ToString(exclude = "room")
@AllArgsConstructor
@NoArgsConstructor
public class Topic extends BaseEntity {

    @Column(name = "user_int_id", nullable = false)
    Long userIntId;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "reg_date", nullable = false)
    LocalDateTime regDate;

    @Column(name = "reg_year", nullable = false)
    int year;

    @Column(name = "recommand")
    long recommand;

    @Column(name = "reg_weeknum", nullable = false)
    int weekNum;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;

    public Topic(Long userIntId, String title, LocalDateTime regDate, boolean isDeleted) {
        this.userIntId = userIntId;
        this.title = title;
        this.regDate = regDate;
        this.year = regDate.getYear();
        this.weekNum = regDate.get(WeekFields.ISO.weekOfYear());
        this.isDeleted = isDeleted;
    }

    @OneToMany(mappedBy = "topic")
    List<Vote> voters = new ArrayList<>();

    @OneToMany(mappedBy = "topic")
    List<Room> rooms = new ArrayList<>();

    public boolean check(VoteDTO dto) {
        for (Vote v : this.voters) {
            if (v.getTopic().getId().equals(dto.getTopicId()) && v.getMemberId().equals(dto.getUserId()) && !v.isDelete()) {
                return true;
            }
        }
        return false;
    }

    public long count() {
        long cnt = 0;
        for (Vote v : this.voters) {
            if (!v.isDelete()) cnt++;
        }
        this.recommand = cnt;
        return cnt;
    }


}
