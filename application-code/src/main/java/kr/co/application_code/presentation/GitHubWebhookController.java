package kr.co.application_code.presentation;

import kr.co.application_code.application.GitHubPullRequestWebhookExtractor;
import kr.co.application_code.domain.PullRequestMetadata;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/github")
public class GitHubWebhookController {

    private final GitHubPullRequestWebhookExtractor extractor;

    public GitHubWebhookController(GitHubPullRequestWebhookExtractor extractor) {
        this.extractor = extractor;
    }

    /**
     * 실제 배포 후 GitHub Webhook에서 이 엔드포인트로 POST가 들어오게 된다.
     * 지금 단계에서는 로컬에서 curl로도 호출 가능
     */
    @PostMapping(
            value = "/pull-request",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PullRequestMetadata onPullRequest(@RequestBody String payloadJson) {
        return extractor.extract(payloadJson);
    }
}
