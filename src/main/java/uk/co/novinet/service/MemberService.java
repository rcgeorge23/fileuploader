package uk.co.novinet.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static uk.co.novinet.service.PersistenceUtils.*;

@Service
public class MemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateOrCreate(Member member) {
        Member existingMember = findMemberByToken(member.getToken());

        if (existingMember == null) {
            LOGGER.info("No member found for token: " + member.getToken() + " so going to create a new one.");
            createNewMemberEnquiry(member);
            return;
        }

        LOGGER.info("Going to update member: {}", member);

        String sql = "update " + usersTableName() + " u " +
                "set u.mp_name = ?, " +
                "u.mp_engaged = ?, " +
                "u.mp_sympathetic = ?, " +
                "u.mp_constituency = ?, " +
                "u.mp_party = ?, " +
                "u.schemes = ?, " +
                "u.industry = ?, " +
                "u.name = ?, " +
                "u.email = ?, " +
                "u.has_completed_membership_form = ?, " +
                "u.how_did_you_hear_about_lcag = ?, " +
                "u.member_of_big_group = ?, " +
                "u.big_group_username = ?, " +
                "u.document_upload_error = ? " +
                "where u.token = ?";

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                member.getMpName(),
                member.getMpEngaged(),
                member.getMpSympathetic(),
                member.getMpConstituency(),
                member.getMpParty(),
                member.getSchemes(),
                member.getIndustry(),
                member.getName(),
                member.getEmailAddress(),
                member.hasCompletedMembershipForm(),
                member.getHowDidYouHearAboutLcag(),
                member.getMemberOfBigGroup() == null ? "" : member.getMemberOfBigGroup(),
                member.getBigGroupUsername(),
                member.getDocumentUploadError(),
                member.getToken()
        );

        LOGGER.info("Update result: {}", result);
    }

    private void createNewMemberEnquiry(Member member) {
        LOGGER.info("Going to insert enquiry: {}", member);

        String sql = "insert into " + enquiryTableName() +
                " (`id`, `name`, `email_address`, `mp_name`, `mp_constituency`, `mp_party`, `mp_engaged`, `mp_sympathetic`, `schemes`, " +
                "`industry`, `how_did_you_hear_about_lcag`, `member_of_big_group`, `big_group_username`, `has_been_processed`, `date_created`) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";


        Long nextAvailableId = findNextAvailableId("id", enquiryTableName());

        LOGGER.info("Created sql: {}", sql);

        int result = jdbcTemplate.update(
                sql,
                nextAvailableId,
                member.getName(),
                member.getEmailAddress(),
                member.getMpName(),
                member.getMpConstituency(),
                member.getMpParty(),
                member.getMpEngaged(),
                member.getMpSympathetic(),
                member.getSchemes(),
                member.getIndustry(),
                member.getHowDidYouHearAboutLcag(),
                member.getMemberOfBigGroup() == null ? false : member.getMemberOfBigGroup(),
                member.getBigGroupUsername(),
                false,
                unixTime(Instant.now())
        );

        LOGGER.info("Update result: {}", result);
    }

    public Member findMemberByToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        List<Member> members = jdbcTemplate.query("select * from " + usersTableName() + " u where lower(u.token) = ?", new Object[] { token.toLowerCase() }, (rs, rowNum) -> buildMember(rs));

        if (members == null || members.size() == 0) {
            return null;
        }

        if (members.size() > 1) {
            LOGGER.error("More than one member found for token: {}, members: ", token, members);
            throw new RuntimeException("More than one member found for token: " + token);
        }

        return members.get(0);
    }

    private Member buildMember(ResultSet rs) throws SQLException {
        return new Member(
                rs.getLong("uid"),
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("mp_name"),
                rs.getString("schemes"),
                rs.getBoolean("mp_engaged"),
                rs.getBoolean("mp_sympathetic"),
                rs.getString("mp_constituency"),
                rs.getString("mp_party"),
                rs.getString("industry"),
                rs.getString("token"),
                rs.getBoolean("has_completed_membership_form"),
                rs.getBoolean("member_of_big_group"),
                rs.getString("how_did_you_hear_about_lcag"),
                rs.getString("big_group_username"),
                rs.getBoolean("hmrc_letter_checked"),
                rs.getBoolean("identification_checked"),
                rs.getBoolean("document_upload_error")
        );
    }

}
