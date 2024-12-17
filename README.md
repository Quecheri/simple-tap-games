
# Simple Tap Games

[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/Quecheri/simple-tap-games)](https://img.shields.io/github/v/release/Quecheri/simple-tap-games)
[![GitHub last commit](https://img.shields.io/github/last-commit/Quecheri/simple-tap-games)](https://img.shields.io/github/last-commit/Quecheri/simple-tap-games)
[![GitHub issues](https://img.shields.io/github/issues-raw/Quecheri/simple-tap-games)](https://img.shields.io/github/issues-raw/Quecheri/simple-tap-games)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/Quecheri/simple-tap-games)](https://img.shields.io/github/issues-pr/Quecheri/simple-tap-games)
[![GitHub](https://img.shields.io/github/license/Quecheri/simple-tap-games)](https://img.shields.io/github/license/Quecheri/simple-tap-games)

The project uses Bluetooth communication to create several simple games that can be played on multiple phones.

# Table of Contents
- [Project Title](#project-title)
- [Table of Contents](#table-of-contents)
- [Installation](#installation)
- [Usage](#usage)
- [License](#license)


# Installation
[(Back to top)](#table-of-contents)

> **Note**: To use app only .apk file is required

if you are interested in source code

```shell
gh repo clone Quecheri/simple-tap-games
```
 > **Requirements**: A phone with Android 8.0 or newer and Bluetooth functionality.
> 
 > **Note**: We strongly recommend using Android Studio version 2024.1.2. or newer.

# Usage
[(Back to top)](#table-of-contents)
## Nim
The number of players equals the number of phones. The screen displays the number of remaining matches. The objective of the game is to take matches in such a way that you are not left with the last one. During their turn, a player can take 1 to 3 matches by pressing the corresponding button. The game continues until the last match is taken. The player who takes the last match loses. If a player does not choose the number of matches within the allotted time, their turn is skipped, and they automatically take one match.

<div align="center">
<img src="https://github.com/Quecheri/simple-tap-games/blob/master/Preview/Nim1.jpg?raw=true" width="250" align="center">
<img src="https://github.com/Quecheri/simple-tap-games/blob/master/Preview/Nim2.jpg?raw=true" width="250" align="center">
</div>

## Reaction Speed
A single-player game. On one of the connected devices, an image of either a capybara or a beaver is displayed. The player's goal is to click on the screen as quickly as possible if a beaver appears or avoid clicking if a capybara appears. If the player does not press any screen within the specified time, it is considered a wrong reaction for a beaver and a correct reaction for a capybara. After the game ends, a summary of reaction times is displayed.

<div align="center">
<img src="https://github.com/Quecheri/simple-tap-games/blob/master/Preview/Capibara.jpg?raw=true" width="250" align="center">
<img src="https://github.com/Quecheri/simple-tap-games/blob/master/Preview/Beaver.jpg?raw=true" width="250" align="center">
</div>

## Combinations
A single-player game. At the start, a sequence is displayed using yellow flashes. The player must memorize the order of flashing devices and then replicate it by clicking on the correct devices in the right order. A correct selection is indicated with a green flash, while an incorrect one is marked with a red flash. After each successful round, the sequence grows by one element. The game ends after a wrong click or after completing all rounds, and a summary is then displayed.


# License
[(Back to top)](#table-of-contents)

This project uses the following open source libraries:

Nordic Semiconductor library (Copyright (c) 2015, Nordic Semiconductor)
BSD 3-Clause License 
See the [Nordic LICENSE](./Nordic%20LICENSE) file for more details.

Project Under license:
[BSD 3-Clause License](./LICENSE)


