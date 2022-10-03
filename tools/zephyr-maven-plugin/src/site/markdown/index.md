# Introducing the Zephyr Installer Plugin Ecosystem

Generating configurable, cross-platform self-extracting executables has been
the purview of commercial software for, well, forever.  The Zephyr Build plugins
project aims to change that.

With Zephyr Build Plugins, you can generate any self-extracting executable
targeting any supported platform from any supported platform, as well as
executables for every supported platform on any supported platform from
the same build.

Generated executables do not require any JVM to be installed on the end
user's system to function.

## Feature Matrix


|                            | Windows | Mac         | Linux |
|----------------------------|---------|-------------|-------|
| Self-Extracting Executable | Yes     | In-Progress | Yes   |
| Code-Signing               | Yes     | In-Progress | Yes   |
| Executable Icon            | Yes     | In-Progress | Yes   |
| Executable Metadata        | Yes     | In-Progress | Yes   |
| Executable Run Permissions | Yes     | In-Progress | Yes   |

Additionally, Zephyr Installer Plugins can generate ICO and ICNS icons
from PNG, SVG, and other formats.

## Build System Support


|        | Mac OSX    | Windows    | Linux      |
|--------|------------|------------|------------|
| Maven  | In-Progress| Yes        | Yes        |
| Gradle | Planned    | Planned    | Planned    |
| Bazel  | Commercial | Commercial | Commercial |
| Ant    | Commercial | Commercial | Commercial |


## Use-Cases
####  Creating a self-extracting executable targeting (Windows, Mac OSX, or Linux)
    - can be built on your current operating system (Mac OSX, Windows, or Linux)
    with no modifications or 3rd-party requirements

####  Signing generated executables
    - Executables can be signed for any supported platform with the same configuration
    i.e. Sign Windows executables with Authenticode on MacOSX, Linux, or Windows, or sign MacOSX app packages with CodeSign on
    Mac OSX, Linux, or Windows


####  Generating ICO/ICNS files
    - Traditionally, having ICO or ICNS imageData files on hand has been a prerequisite, requiring 3rd-party
    commercial tools, online imageData generators, etc.  The Zephyr build ecosystem allows you to generate ICO/ICNS files
    from standard raster formats with a variety of sizes

####  Attaching ICO/ICNS files to your executable
    - Inserting branding icons into executables has been a platform-dependent chore,
    but Zephyr allows you to brand your generated executable in a platform-independent way

####  Creating installers for JVM-based programs that don't require downloading the JVM or forcing the end-user to install it
    - Zephyr allows you to launch IzPack installers using a JVM bundled with your application

#### Automate everything!
    - Since these tools are included as build plugins for the most popular build systems, you can
    completely eliminate any manual steps in your installer process!


## Get Started

Maven: [Get Started with Maven](./maven/getting-started.html)

