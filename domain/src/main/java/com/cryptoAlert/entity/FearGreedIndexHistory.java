package com.cryptoAlert.entity;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
public class FearGreedIndexHistory {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "index_value")  // 예약어 피하기 위해 DB 컬럼명을 명시
    private int value; // 0~100
    private String classification; // Extreme Fear, Fear, Neutral, Greed, Extreme Greed
    private LocalDateTime timestamp;
}

