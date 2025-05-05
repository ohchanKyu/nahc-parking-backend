package kr.ac.dankook.parkingApplication.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String password;
    private String email;
    private String name;
    private String roles;

    public List<String> getRoleList(){
        if (!this.roles.isEmpty()){
            return Arrays.asList(this.roles.split(","));
        }
        return List.of();
    }
}
