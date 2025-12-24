# Contributing

Thank you for your interest in the project! Below are some guidelines for contributors.

## 🚀 How to get started
1. Fork the repository
2. Create a branch: `git checkout -b feature/name` or `fix/name`
3. Make changes and commit: `git commit -m "Description"`
4. Push the branch: `git push origin feature/name`
5. Create a Pull Request

- If possible, try to duplicate changes for older versions of the game so that the mod update is relevant for them as well.

## 🧪 Testing
- Make sure that the added/fixed functionality works correctly and does not break existing mod functions.
- Make sure the mod starts and works correctly on both the server and client side **not only in the IDE, but also in the actual working environment**

## 💡 Code Style
- Use clear variable and function names
- Follow the existing codestyle and project architecture principles.
- If a class contains a lot of functions, it might be worth splitting it into several separate files.
- If a package contains many files, it may be worth separating some of the classes into one common subpackage.
- Format code before PR

---

# Mod update for the new version of the game

The following flowchart is currently in use:

1. Clone the current branch for the latest version of the game.
   
3. In the root `gradle.properties` file, change the following values ​​to match the version you want to port the mod to:
    - `minecraft_version`
    - `fabric_loader_version`
    - `fabric_api_version`
    - `neoforge_version`
      
4. In the root `build.gradle` file, in the `plugins` dependency specification (at the very top of the file), change the version of `dev.architectury.loom` to the current version.
   
5. Try building the project to update all dependencies and mappings. If you try to build with them, you'll get compatibility errors between the old version and the new one.
  
6. Fix all compatibility errors to preserve the mod's functionality.

- ℹ️ Compatibility errors are most likely to occur in classes that interact with the game's `net.minecraft.*` classes. When upgrading to a new version of Minecraft, the names and signatures of classes and methods often change, and sometimes some classes may be removed. Almost all changes are usually detailed here: https://docs.neoforged.net/primer/docs/
  
- ⚠️ When upgrading, it's important to test the full functionality of each Mixin class (classes in all `mixin` packages). If necessary, rewrite the implementation of functionality to reflect changes in the original game classes.
  
6. Ensure that all conditions in the `#Testing` section of this guide are met.
  
7. Submit a Pull Request to create a new branch in the original repository named after the Minecraft version to which the mod was ported.

ℹ️ Following this scheme is optional if you have a more convenient and practical way to upgrade to the new version.

---

## 📄 License
By submitting code, you agree that it will be distributed under the current project license.
