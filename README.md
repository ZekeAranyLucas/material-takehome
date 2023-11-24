# Material Security Takehome Interview: In-Memory Filesystem

> # Prompt
>
> Build an in-memory filesystem! This is a simplified file system that supports both files and directories. You don’t have to work with any actual files, everything will just be contained in-memory.
>
> Write functions that correspond to familiar file system commands. We only care about the capabilities, so you may choose to combine multiple capabilities into one function, or even split up one capability amongst multiple functions (up to you). You can leave comments and TODOs if there are ideas or issues you want to discuss or point out, but don’t have time to implement/fix, or aren’t quite sure how.

## Structural requirements

- [x] Choose a language of your choice (excluding bash, haskell, or scala). **Java selected.**
- [] Feel free to use small libraries where appropriate instead of re-inventing the wheel.
- [] Feel free to look things up or use other reference material as you would when working
  normally
- [] Please make this look as close to real production code you would submit for code review
  (e.g feel free to refactor aggressively, use helper functions, etc)
- [x] Submit your code via github. **https://github.com/ZekeAranyLucas/material-takehome**
- [] Please include a README that includes how to get your
  code running / tested.

## Functional requirements

- [x] Change the current working directory. The working directory begins at '/'. You may traverse to a child directory or the parent.
- [x] Get the current working directory. Returns the current working directory's path from the root to console. Example: ‘/school/homework’
- [x] Create a new directory. The current working directory is the parent.
- [x] Get the directory contents: Returns the children of the current working directory.
      Example: [‘math’, ‘history’, ‘spanish’]
- [x] Remove a directory. The target directory must be among the current working directory’s
      children.
- [x] Create a new file: Creates a new empty file in the current working directory.
- [x] Write file contents: Writes the specified contents to a file in the current working
      directory. All file contents will fit into memory.
- [x] Get file contents: Returns the content of a file in the current working directory.
- [] Move a file: Move an existing file in the current working directory to a new location (in
  the same directory).
- [] Find a file/directory: Given a filename, find all the files and directories within the current
  working directory that have exactly that name.
- [] Interface with Java's Files.\* APIs.

# Architecture

![High Level Design](imfs-high-level-design.png)

[See FigJam](https://www.figma.com/file/DNAxxIspJKGBQUapbo3fhG/Untitled?type=whiteboard&node-id=1%3A2&t=hDWt2koQ75jWMFpJ-1)

## Decision records

### D1: Use Codespaces

This is an opportunity to experiment. The hypothesis is that Codespaces will it easier to share the code and execution environment with reviewers at Material, so that it's less effort to dig in and evaluate the specifics of the project.

### D2: Implement Java's FileSystemProvider (FSP)

Java has defined how to integrate as a file system. The hypothesis is that [FSP is a good abstraction](https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/filesystemprovider.html) for
representing an in-memory file system, and will also allow richer scenarios pretty quickly through
the commonly used and documented [Files API](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html).

### D3: Separate relative state into a context object

State like the working directory is relative to the caller and not global to the storage. They hypothesis is that
this state is better kept explicitly under the caller's control instead of process global (like env vars)
or thread local, so that we avoid future problems with multiple simultaneous calllers.

NOTE: Java's working dir for the default file system is not fully mutable,
so would clearly not meet the requirements of this exercise.

### D4: Use Materialized paths as storage

Materialized paths are where each row contains the full path as the primary key
instead of doing something with graphs or nested objects. The hypothesis is that understanding and debugging
will be simpler than the other options. It makes it pretty easy to trade between different storage options
like arraylist, hashmaps, or even SQL tables.

### D5: Support arbitrary volumes

While the basic requirements are for a single global in-memory store, it's trivial to support multiple.
The hypothesis is that it will make it easier to do isolated testing, and maybe try alternate storage options.
The first alternative is "\*Test" namespaces will start with children: [‘math’, ‘history’, ‘spanish’].

### D6: Don't be pedantic about unit testing

Developer testing can include all kinds of test variations. Hypothesis is that strict isolation won't
help iron out kinks in the design, but could slow down development by forcing mocks for singletons, etc.

### D7: Blob storage for files has no limits

Commiting the file blobs as byte arrays means the system can become unpredictable, so this is risky.
Hypothesis is that we simplify the implementation to meet the functional requirments, but we will
need to revisit limits and the implmentation before general purpose release. once the scenarios
are understood, then limits will need to be tested and enforced.

# TODO : slim down devcontainers stuff

# Try Out Development Containers: Java

[![Open in Dev Containers](https://img.shields.io/static/v1?label=Dev%20Containers&message=Open&color=blue&logo=visualstudiocode)](https://vscode.dev/redirect?url=vscode://ms-vscode-remote.remote-containers/cloneInVolume?url=https://github.com/microsoft/vscode-remote-try-java)

A **development container** is a running container with a well-defined tool/runtime stack and its prerequisites. You can try out development containers with **[GitHub Codespaces](https://github.com/features/codespaces)** or **[Visual Studio Code Dev Containers](https://aka.ms/vscode-remote/containers)**.

This is a sample project that lets you try out either option in a few easy steps. We have a variety of other [vscode-remote-try-\*](https://github.com/search?q=org%3Amicrosoft+vscode-remote-try-&type=Repositories) sample projects, too.

> **Note:** If you already have a Codespace or dev container, you can jump to the [Things to try](#things-to-try) section.

## Setting up the development container

### GitHub Codespaces

Follow these steps to open this sample in a Codespace:

1. Click the **Code** drop-down menu.
2. Click on the **Codespaces** tab.
3. Click **Create codespace on main**.

For more info, check out the [GitHub documentation](https://docs.github.com/en/free-pro-team@latest/github/developing-online-with-codespaces/creating-a-codespace#creating-a-codespace).

### VS Code Dev Containers

If you already have VS Code and Docker installed, you can click the badge above or [here](https://vscode.dev/redirect?url=vscode://ms-vscode-remote.remote-containers/cloneInVolume?url=https://github.com/microsoft/vscode-remote-try-java) to get started. Clicking these links will cause VS Code to automatically install the Dev Containers extension if needed, clone the source code into a container volume, and spin up a dev container for use.

Follow these steps to open this sample in a container using the VS Code Dev Containers extension:

1. If this is your first time using a development container, please ensure your system meets the pre-reqs (i.e. have Docker installed) in the [getting started steps](https://aka.ms/vscode-remote/containers/getting-started).

2. To use this repository, you can either open the repository in an isolated Docker volume:

   - Press <kbd>F1</kbd> and select the **Dev Containers: Try a Sample...** command.
   - Choose the "Java" sample, wait for the container to start, and try things out!
     > **Note:** Under the hood, this will use the **Dev Containers: Clone Repository in Container Volume...** command to clone the source code in a Docker volume instead of the local filesystem. [Volumes](https://docs.docker.com/storage/volumes/) are the preferred mechanism for persisting container data.

   Or open a locally cloned copy of the code:

   - Clone this repository to your local filesystem.
   - Press <kbd>F1</kbd> and select the **Dev Containers: Open Folder in Container...** command.
   - Select the cloned copy of this folder, wait for the container to start, and try things out!

## Things to try

Once you have this sample opened, you'll be able to work with it like you would locally.

Some things to try:

1. **Edit:**

   - Open `src/main/java/com/mycompany/app/App.java`.
   - Try adding some code and check out the language features.
   - Make a spelling mistake and notice it is detected. The [Code Spell Checker](https://marketplace.visualstudio.com/items?itemName=streetsidesoftware.code-spell-checker) extension was automatically installed because it is referenced in `.devcontainer/devcontainer.json`.
   - Also notice that the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) is installed. The JDK is in the `mcr.microsoft.com/devcontainers/java` image and Dev Container settings and metadata are automatically picked up from [image labels](https://containers.dev/implementors/reference/#labels).

2. **Terminal:** Press <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>\`</kbd> and type `uname` and other Linux commands from the terminal window.

3. **Build, Run, and Debug:**

   - Open `src/main/java/com/mycompany/app/App.java`.
   - Add a breakpoint.
   - Press <kbd>F5</kbd> to launch the app in the container.
   - Once the breakpoint is hit, try hovering over variables, examining locals, and more.

4. **Run a Test:**

   - Open `src/test/java/com/mycompany/app/AppTest.java`.
   - Put a breakpoint in a test.
   - Click the `Debug Test` in the Code Lens above the function and watch it hit the breakpoint.

5. **Install Node.js using a Dev Container Feature:**
   - Press <kbd>F1</kbd> and select the **Dev Containers: Configure Container Features...** or **Codespaces: Configure Container Features...** command.
   - Type "node" in the text box at the top.
   - Check the check box next to "Node.js (via nvm) and yarn" (published by devcontainers)
   - Click OK
   - Press <kbd>F1</kbd> and select the **Dev Containers: Rebuild Container** or **Codespaces: Rebuild Container** command so the modifications are picked up.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## License

Copyright © Microsoft Corporation All rights reserved.<br />
Licensed under the MIT License. See LICENSE in the project root for license information.
