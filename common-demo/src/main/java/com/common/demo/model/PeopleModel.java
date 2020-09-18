package com.common.demo.model;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PeopleModel {
    private String id;
    private String name;
    private int age;
    private String address;
}
