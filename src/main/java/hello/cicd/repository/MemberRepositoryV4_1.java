package hello.cicd.repository;

import hello.cicd.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Slf4j
public class MemberRepositoryV4_1 implements MemberRepository{

    private final JdbcTemplate template;

    public MemberRepositoryV4_1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }


    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?,?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;

    }
    public Member findById(String memberId) {
        String sql = "select * from member where member_id=?";
        return template.queryForObject(sql, memberRowMapper(), memberId);

    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };

    }

    public void update(String memberId, int money)  {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);

    }

    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        template.update(sql, memberId);
    }

}
