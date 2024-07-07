# awsm-credentials-gradle
Get AWS credentials for S3 maven repos from default AWS credentials provider chain

# ⚠️ This project is deprecated ⚠️

Gradle now supports S3 maven repos out-of-the-box:
* [Official Documentation](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:s3-repositories)
* Blog post: [Using an AWS S3 Bucket as your Maven repository in a Gradle project](https://kevcodez.de/posts/2020-02-02-s3-maven-repository-gradle/)

## Usage

1. Add this plugin to your project, see https://plugins.gradle.org/plugin/com.github.kaklakariada.awsm-credentials-gradle

    ```gradle
    plugins {
      id "com.github.kaklakariada.awsm-credentials-gradle" version "0.2.1"
    }
    ```
2. Configure your AWS credentials as [named profiles](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-multiple-profiles) in `~/.aws/credentials` (*not* in `~/.aws/config`). Use `role_arn` and `source_profile` for [role delegation](http://docs.aws.amazon.com/cli/latest/topic/config-vars.html#using-aws-iam-roles):

    ```ini
    [root-credentials]
    aws_access_key_id = ...
    aws_secret_access_key = ...
    
    [delegate-account1]
    role_arn = arn:aws:iam::<account>:role/<role name>
    source_profile = root-credentials
    
    [delegate-account2]
    role_arn = arn:aws:iam::<account>:role/<role name>
    source_profile = root-credentials
    ```
3. Configure the AWS profile you want to use in `~/.gradle/gradle.properties`:

    ```properties
    systemProp.aws.profile = delegate-account1
    ```
4. Add S3 maven repositories without specifying credentials. The plugin will automatically add credentials for repositories with `s3://` urls in all projects.
  * Dependency repositories:

    ```gradle
    repositories {
       maven {
           url "s3://bucket/path/to/repo"
       }
    }
    ```
  * Publishing repositories:

    ```gradle
    plugins {
        id "maven-publish"
    }
    publishing {
        repositories {
            maven {
                url "s3://bucket/path/to/repo"
            }
        }
        publications {
            // ...
        }
    }
    ```


## Development

```bash
$ git clone https://github.com/hamstercommunity/awsm-credentials-gradle.git
```

### Using eclipse

Import into eclipse using [buildship](https://projects.eclipse.org/projects/tools.buildship) plugin:

1. Select File > Import... > Gradle > Gradle Project
2. Click "Next"
3. Select Project root directory
4. Click "Finish"

### Generate license file header

```bash
$ ./gradlew licenseFormatMain licenseFormatTest
```

### Publish to [plugins.gradle.org](https://plugins.gradle.org)

See https://plugins.gradle.org/docs/submit for details.

1. Add API Key from https://plugins.gradle.org to `~/.gradle/gradle.properties`:

    ```
    gradle.publish.key = ...
    gradle.publish.secret = ...
    ```
2. Run `./gradlew publishPlugins`
