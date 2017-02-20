# awsm-credentials-gradle
Get AWS credentials for S3 maven repos from default AWS credentials provider chain

## Usage

1. Add this plugin to your project, see https://plugins.gradle.org/plugin/com.github.kaklakariada.awsm-credentials-gradle
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

### Generate license header for added files:

```bash
$ ./gradlew licenseFormatMain licenseFormatTest
```

### Publish to [plugins.gradle.org](https://plugins.gradle.org)

See https://plugins.gradle.org/docs/submit for details.

1. Add API Key for https://plugins.gradle.org to `~/.gradle/gradle.properties`
2. Run `./gradlew publishPlugins`
