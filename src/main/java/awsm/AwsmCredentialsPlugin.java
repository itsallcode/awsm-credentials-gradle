/**
 * awsm-credentials-gradle - Get AWS credentials for S3 maven repos from default AWS credentials provider chain
 * Copyright (C) 2017 Hamster community <christoph at users.sourceforge.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package awsm;

import java.net.URI;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.credentials.AwsCredentials;
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
        project.getAllprojects().forEach(this::applyForProject);
    }

    public void applyForProject(Project project)
    {
        project.getRepositories().all(this::repositoryAdded);
        project.afterEvaluate(p -> {
            project.getRepositories().all(this::repositoryAdded);
            final PublishingExtension publishingExtension = project.getExtensions().findByType(PublishingExtension.class);
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
        logger.info("Got AWS credentials {} with access key {}", awsCredentials, awsCredentials.getAWSAccessKeyId());
        gradleAwsCredentials = createGradleAwsCredentials(awsCredentials);
        return gradleAwsCredentials;
    }

    private static DefaultAwsCredentials createGradleAwsCredentials(final AWSCredentials awsCredentials)
    {
        final DefaultAwsCredentials credentials = new DefaultAwsCredentials();
        credentials.setAccessKey(awsCredentials.getAWSAccessKeyId());
        credentials.setSecretKey(awsCredentials.getAWSSecretKey());
        if (awsCredentials instanceof AWSSessionCredentials)
        {
            credentials.setSessionToken(((AWSSessionCredentials) awsCredentials).getSessionToken());
        }
        return credentials;
    }
}
