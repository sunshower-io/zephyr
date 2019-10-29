# Sunshower Kernel

The Sunshower kernel is a flexible microkernel allowing developers to quickly develop, test, and deploy
individual applications.


## Overview
## User Interface
## File System
This is an overview of the Sunshower.io Kernel File System

### Structure
The Kernel filesystem is structured as follows:

$SUNSHOWER_HOME = /

When a kernel module is installed, a new "directory" is created at:
droplet://<droplet-group>/<droplet-name>/<droplet-version> 
where `<droplet-group>`, `<droplet-name>` and `<droplet-version>` correspond to the following `META-INF/MANIFEST.MF` 
entries of the installed assembly

`version` (must be lower-case)
`name` (lower-case)
`group` (lower-case)


#### ROOT
the directory `droplet:///` corresponds to the root of the Sunshower.io file-system
and may only be accessed by kernel modules. There are several notable files and directories
in this filesystem:

1. `kernel.idx`--an index of the installed plugins and kernel-modules, as well as information about location, digests, and state
1. `modules/` a directory containing the list (symlinked) of installed modules
1. `plugins/` a directory containing the list (symlinked) of installed plugins


#### Module URI structure
Given a module with `group=sunshower:artifact=stuff:version=1.0.0`, the module's structure
can be located at `droplet:///sunshower/artifact/1.0.0`.  Everything within this URI corresponds to the physical directory structure
of the assembly installed.   The Sunshower kernel creates several additional files at this scheme:

1.  `plugin.idx`: `droplet:///sunshower/artifact/1.0.0/plugin.idx` -- this file contains kernel-specific information and must not be modified
1.  `plugin.info`: `droplet:///sunshower/artifact/1.0.0/plugin.info` -- this file contains information about this plugin and its state
1   `paths.idx` : `etc.` --this file contains kernel-specific information about the plugin

#### Phases
Some components in the Sunshower.io Kernel manage operations in phases, which allows for extensibility, 
traceability, and simplicity.

For instance, consider the `DefaultModuleManager`.  A user or process provides an
installation request, at which the following phases are executed:

1. The ModuleDownloadPhase downloads the module to the Kernel temporary storage folder and dispatches the following events
   1. DownloadStarted
   1. DownloadProgressed
   1. DownloadComplete
2. The ModuleUnpackPhase   


### Modules vs Plugins
Modules and plugins share many similarities, but differ in important ways.  
For instance, any kernel-module's classpath is visible to the kernel.
Second, kernel-modules are loaded before any plugins are.



#### Kernel Module Registry
when a kernel module is installed, the kernel saves the entry to droplet://kernel/registry.modlist
upon starting the kernel, modules are read from this list 


#### Kernel Module Lifecycle
The kernel module lifecycle is designed so that kernel module functionality and classpaths become available
at the very earliest opportunity.  The lifecycle is as follows:

1. Kernel Start
    1. Kernel Filesystem is created
    1. `droplet://kernel/modules.list` is read, producing list of existing kernel modules
    1. Kernel classloader is created as the combination of all kernel modules 
    1. Kernel loads all existing modules (initially none), sorted by `order`
    1. Plugin set is loaded
    
#### modules.list
The modules.list file contains the minimum amount of information required to load the kernel modules, and has the following format
```
<order>:<module-group>:<module-name>:<module-version>:[directory-list]
```

    
