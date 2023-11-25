# Material Security Takehome Interview: In-Memory Filesystem

> ### Prompt
>
> Build an in-memory filesystem! This is a simplified file system that supports both files and directories. You don’t have to work with any actual files, everything will just be contained in-memory.
>
> Write functions that correspond to familiar file system commands. We only care about the capabilities, so you may choose to combine multiple capabilities into one function, or even split up one capability amongst multiple functions (up to you). You can leave comments and TODOs if there are ideas or issues you want to discuss or point out, but don’t have time to implement/fix, or aren’t quite sure how.

## Structural parameters

- [x] Choose a language of your choice (excluding bash, Haskell, or scala). **-- Java selected.**
- [x] Feel free to use small libraries where appropriate instead of reinventing the wheel. **-- Lombok for immutable objects.**
- [x] Feel free to look things up or use other reference material as you would when working
      normally. **-- Mostly Java docs for SPI and Google, starting to use copilot.**
- [] Please make this look as close to real production code as you would submit for code review
  (e.g feel free to refactor aggressively, use helper functions, etc)
- [x] Submit your code via Git Hub. **-- See https://github.com/ZekeAranyLucas/material-takehome**
- [x] Please include a README that includes how to get your code running/tested.

## Functional requirements

- [x] Change the current working directory. The working directory begins at '/'. You may traverse to a child directory or the parent.
- [x] Get the current working directory. Returns the current working directory's path from the root to the console. Example: ‘/school/homework’
- [x] Create a new directory. The current working directory is the parent.
- [x] Get the directory contents: Returns the children of the current working directory.
      Example: [‘math’, ‘history’, ‘Spanish’]
- [x] Remove a directory. The target directory must be among the current working directory’s
      children.
- [x] Create a new file: Create a new empty file in the current working directory.
- [x] Write file contents: Writes the specified contents to a file in the current working
      directory. All file contents will fit into memory.
- [x] Get file contents: Returns the content of a file in the current working directory.
- [x] Move a file: Move an existing file in the current working directory to a new location (in
      the same directory).
- [x] Find a file/directory: Given a filename, find all the files and directories within the current
      working directory that have exactly that name. **there can be only one**
- [x] throw when trying to delete non-empty directories.
- [] Interface with Java's `Files.\*` APIs.

## Non-functional requirements

- [x] Move away from ArrayList for ImfsFileSystem before performance starts to suck!
- [] Move away from TreeMap depending on the scenarios.
- [] Limit the size of files to something testable/rational. if the files are big, fragmentation will be a bad problem.
- [] How important is concurrency? consider if Collections.synchronizedSortedMap is needed.

## Extension requirements

Copied from the PDF.

### [x] Move and copy

- [x] You can move or copy files and directories. **-- including to and from the file system!** **See [importFiles](src/main/java/com/imfs/ImfsContext.java).**
- [] Support merging the contents of two directories when moving or copying one into
  the other.
- [] Handle name collisions in some way (e.g. auto renaming files, merging
  directories.)

### [x] Operations on paths

- [x] When doing basic operations (changing the current working directory, creating or moving files or folders, etc), you can use absolute paths instead of only operating on objects in the current working directory. **-- ImfsContext resolves everything to absolute paths anyway.**
- [?] You can use relative paths (relative to the current working directory) as well, including the special “..” path that refers to the parent directory. **-- some relative operations just work.**
- []When creating or moving items to a new path, you can choose to automatically create any intermediate directories on the path that don’t exist yet.

### [x] Walk a subtree

- [x] You can walk through all the recursive contents of a directory, invoking a passed-in function on each child directory/file.
- [x] While walking, the passed-in function can arbitrarily choose not to recurse into certain subdirectories.
- [x] Use this to implement some recursive operations. For example, finding the first file in a subtree whose name matches a regex.
      **-- see [testImportFiles](src/test/java/com/imfs/ImfsContextTest.java)**

NOTE: Traversal is supported explicitly via NIO's [`walk() and walkTree()`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#walk-java.nio.file.Path-java.nio.file.FileVisitOption...-).

# Verifying the requirements

1. **Try Out Codespaces: Java**

   - Follow these steps to open this repo in a Codespace:
   - Click the **Code** drop-down menu.
   - Click on the **Codespaces** tab.
   - Click **Create codespace on main**.
   - For more info, check out the [GitHub documentation](https://docs.github.com/en/free-pro-team@latest/github/developing-online-with-codespaces/creating-a-codespace#creating-a-codespace).
   - Or just treat it like a Java repo.

2. **Run the Tests:**

   - The core functional scenarios are covered here: `src/test/java/com/imfs/ImfsContextTest.java`.
   - Open the file and use the right-click `Run Test` options.
   - Click the `Debug Test` in the Code Lens above the function and watch it hit any breakpoints.

3. **Build, Run, and Debug:**

   - Open `src/main/java/com/mycompany/app/App.java`.
   - Add a breakpoint.
   - Press <kbd>F5</kbd> to launch the app in the container.
   - Once the breakpoint is hit, try hovering over variables, examining locals, and more.

# Architecture

![High-Level Design](imfs-high-level-design.png)

[See FigJam](https://www.figma.com/file/DNAxxIspJKGBQUapbo3fhG/Untitled?type=whiteboard&node-id=1%3A2&t=hDWt2koQ75jWMFpJ-1)

## Decision records

### D1: Use Codespaces

This is an opportunity to experiment. The hypothesis is that Codespaces will it easier to share the code
and execution environment with reviewers at Material so that it's less effort to dig in and evaluate the specifics of the project.

### D2: Implement Java's FileSystemProvider (FSP)

Java has defined how to integrate as a file system. The hypothesis is that [FSP is a good abstraction](https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/filesystemprovider.html) for
representing an in-memory file system, and will also allow richer scenarios pretty quickly through
the commonly used and documented [Files API](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html).

### D3: Separate relative state into a context object

State like the working directory is relative to the caller and not global to the storage. The hypothesis is that
this state is better kept explicitly under the caller's control instead of process global (like env vars)or thread-local so that we avoid future problems with multiple simultaneous callers.

NOTE: Java's implementation of working dir for the default file system is not fully mutable,
so also does not meet the requirements of this exercise.

### D4: Use Materialized paths as storage

Materialized paths are where each row contains the full path as the primary key
instead of doing something with graphs or nested objects. The hypothesis is that understanding and debugging
will be simpler than the other options. It makes it pretty easy to trade between different storage options like ArrayList, hashmaps, or even SQL tables.

### D5: Support arbitrary volumes

While the basic requirements are for a single global in-memory store, it's trivial to support multiple.
The hypothesis is that it will make it easier to do isolated testing, and maybe try alternate storage options.
The first alternative is "\*Test" namespaces will start with child directories: `[‘math’, ‘history’, ‘Spanish’]`.

### D6: Don't be pedantic about unit testing

Developer testing can include all kinds of test variations. The hypothesis is that strict isolation won't
help iron out kinks in the design, but could slow down development by forcing mocks for singletons, etc.

### D7: Blob storage for files has no limits

Committing the file blobs as byte arrays means the system can become unpredictable, so this is risky.
The hypothesis is that we simplify the implementation to meet the functional requirements,
but we will need to revisit limits and the implementation before the general purpose release.
once the scenarios are understood, then limits will need to be tested and enforced.

### D8: defer support for relative paths

Relative paths are required to use some `Files.*` APIs (like [createDirectories](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#createDirectories-java.nio.file.Path-java.nio.file.attribute.FileAttribute...-)).
Hypothesis the leverage for doing this right now is weak, and the time is better spent on other scenarios. Relative paths are also one of the suggested extensions. The prototype code is in the branch fix-mkdirs-and-relative-paths.

### D9: Upgrade TreeMap as the storage index

ArrayList and HashMap were easy to use, but bad for any child enumerations, since they
required going through the entire store for any enumeration. The hypothesis is
that TreeMap will be good enough until we understand the usage patterns for put/get/enum/io.

- Checking for empty directories (enum kids) is now reasonably cheap.
- put/get is still faster than ArrayList, but slower than HashMap.
- enumerating the root still requires iterating over the full set and filtering.

Most likely the next step in indexes would be to use H2, SQLite, or another in-memory RDB.
That would make these queries possible: `SELECT * WHERE path LIKE 'foo/%' and path NOT LIKE 'foo/%/%'` or for the root `SELECT * WHERE path LIKE '%' and path NOT LIKE '%/%'`
since root enums are the worst case scenario.

Or switching to something like Nested Set or Adjacency List.

## License

Licensed under the MIT License. See LICENSE in the project root for license information.
