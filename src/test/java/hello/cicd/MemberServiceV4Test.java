package hello.cicd;

import hello.cicd.domain.Member;
import hello.cicd.repository.MemberRepository;
import hello.cicd.repository.MemberRepositoryV4_1;
import hello.cicd.service.MemberServiceV4;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest
class MemberServiceV4Test {

    private static final String MEMBER_A = "member_A";
    private static final String MEMBER_B = "member_B";
    private static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberServiceV4 memberService;


    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AopCheck() {
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer(){
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        //when

        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(8000);
        Assertions.assertThat(findMemberB.getMoney()).isEqualTo(12000);

    }


    @Test
    @DisplayName("이체 중 예외 밸생")
    void exceptionOccur()  {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEX = new Member(MEMBER_EX, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberEX);
        //when

        Assertions.assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);


        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEX = memberRepository.findById(memberEX.getMemberId());

        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(10000);
        Assertions.assertThat(findMemberEX.getMoney()).isEqualTo(10000);

    }
}