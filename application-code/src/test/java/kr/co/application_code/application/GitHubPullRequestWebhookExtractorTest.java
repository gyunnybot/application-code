package kr.co.application_code.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.application_code.domain.PullRequestMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GitHubPullRequestWebhookExtractorTest {

    private final GitHubPullRequestWebhookExtractor extractor =
            new GitHubPullRequestWebhookExtractor(new ObjectMapper());

    @Test
    void extracts_pull_request_metadata_from_opened_event() {
        String json = FixtureLoader.loadUtf8("github-webhook/pull_request_opened.json");

        PullRequestMetadata meta = extractor.extract(json);

        assertThat(meta.repo().owner()).isEqualTo("octocat");
        assertThat(meta.repo().name()).isEqualTo("hello-world");

        assertThat(meta.prNumber()).isEqualTo(1347);
        assertThat(meta.prId()).isEqualTo(1_000_000_001L);

        assertThat(meta.title()).isEqualTo("Improve README");
        assertThat(meta.state()).isEqualTo("open");
        assertThat(meta.merged()).isFalse();

        assertThat(meta.author().login()).isEqualTo("octocat");
        assertThat(meta.baseBranch()).isEqualTo("main");
        assertThat(meta.headBranch()).isEqualTo("feature/readme");

        assertThat(meta.labels()).containsExactly("documentation");
        assertThat(meta.requestedReviewers())
                .extracting(PullRequestMetadata.UserRef::login)
                .containsExactly("reviewer1");
    }
}
