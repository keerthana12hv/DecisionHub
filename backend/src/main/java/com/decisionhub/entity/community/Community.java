package com.decisionhub.entity.community;

import com.decisionhub.entity.authentication.User;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY)
    private List<CommunityMember> members;
}