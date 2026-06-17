with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

deps = '''    implementation(libs.coil.compose)
    implementation(libs.jetbrains.compose.material3)
    implementation(libs.jetbrains.compose.ui)
    implementation(libs.jetbrains.compose.foundation)
    implementation(libs.jetbrains.compose.runtime)
'''

content = content.replace('    implementation(libs.coil.compose)', deps)

plugin_search = r'alias\(libs\.plugins\.android\.application\)'
plugin_replace = r'''alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.compose)'''
content = content.replace(plugin_search, plugin_replace)

compose_config = '''
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
    }
'''

content = content.replace('''    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }''', compose_config)


with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
