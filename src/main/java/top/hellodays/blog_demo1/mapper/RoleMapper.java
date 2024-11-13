package top.hellodays.blog_demo1.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.hellodays.blog_demo1.entity.Role;

@Mapper
public interface RoleMapper {

    int addRole(Role role);

    int deleteRole(Integer id);

    int updateRole(Role role);

    Role getRoleById(Integer id);

    Role getRoleByName(String keyword);


}
