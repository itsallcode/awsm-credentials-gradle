package awsm;

import java.net.URI;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.credentials.AwsCredentials;
import org.gradle.api.credentials.Credentials;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.internal.credentials.DefaultAwsCredentials;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public class AwsmCredentialsPlugin implements Plugin<Project>
{
    private Logger logger;
    private DefaultAwsCredentials gradleAwsCredentials;

    @Override
    public void apply(Project project)
    {
        this.logger = project.getLogger();
        project.afterEvaluate(p -> {
            project.getRepositories().all(this::repositoryAdded);
            final PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
            if (publishingExtension != null)
            {
                publishingExtension.getRepositories().all(this::repositoryAdded);
            }
        });
    }

    public void repositoryAdded(ArtifactRepository repo)
    {
        if (!(repo instanceof DefaultMavenArtifactRepository))
        {
            return;
        }
        final DefaultMavenArtifactRepository mavenRepo = (DefaultMavenArtifactRepository) repo;
        final URI url = mavenRepo.getUrl();
        if (url == null || url.getScheme() == null || !url.getScheme().equals("s3"))
        {
            return;
        }
        if (mavenRepo.getConfiguredCredentials() != null)
        {
            return;
        }
        final AwsCredentials awsCredentials = getAwsCredentials();
        mavenRepo.setConfiguredCredentials(awsCredentials);
        logger.info("Set AWS credentials with access key {} for repo {}", awsCredentials.getAccessKey(), url);
    }

    private AwsCredentials getAwsCredentials()
    {
        if (gradleAwsCredentials != null)
        {
            return gradleAwsCredentials;
        }
        final AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        logger.lifecycle("Got AWS credentials {} with access key {}", awsCredentials, awsCredentials.getAWSAccessKeyId());
        gradleAwsCredentials = new DefaultAwsCredentials();
        gradleAwsCredentials.setAccessKey(awsCredentials.getAWSAccessKeyId());
        gradleAwsCredentials.setSecretKey(awsCredentials.getAWSSecretKey());
        if (awsCredentials instanceof AWSSessionCredentials)
        {
            gradleAwsCredentials.setSessionToken(((AWSSessionCredentials) awsCredentials).getSessionToken());
        }
        return gradleAwsCredentials;
    }
}
