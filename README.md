# FightCam Ported

FightCam is a Fabric client-side camera mod for watching fights between two selected players.

This repository keeps each Minecraft port on its own branch. Use the branch that matches the Minecraft version you want to build or edit.

## Versions

| Minecraft version | Branch |
| --- | --- |
| 1.21.2 | [`1.21.2`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.2) |
| 1.21.3 | [`1.21.3`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.3) |
| 1.21.5 | [`1.21.5`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.5) |
| 1.21.6 | [`1.21.6`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.6) |
| 1.21.7 | [`1.21.7`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.7) |
| 1.21.8 | [`1.21.8`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.8) |
| 1.21.9 | [`1.21.9`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.9) |
| 1.21.10 | [`1.21.10`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.10) |
| 1.21.11 | [`1.21.11`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/1.21.11) |
| 26.1 | [`26.1`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/26.1) |
| 26.1.1 | [`26.1.1`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/26.1.1) |
| 26.1.2 | [`26.1.2`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/26.1.2) |`r`n| 26.2 | [`26.2`](https://github.com/ModderZellior/Fight-Camera-Ported/tree/26.2) |

## Commands

```text
/fightcam players <player1> <player2>
```

Sets the two players that the camera should frame.

```text
/fightcam toggle
```

Toggles FightCam. It is bound to `;` by default.

```text
/fightcam smooth <factor>
```

Sets the camera lerp factor. Values closer to `0` make the camera smoother, while values closer to `1` make it more reactive. The default is `0.6`.

```text
/fightcam distance <distance>
```

Sets the camera distance from the anchor position in blocks and changes the distance mode to static.

```text
/fightcam distance auto
```

Makes the camera adjust its distance automatically to keep both players in view. The base distance can be adjusted with the forward and backward keys.

```text
/fightcam height <height>
```

Sets the camera height relative to the anchor position in blocks.

```text
/fightcam height avg
```

Makes the camera follow the average height of the selected players.

```text
/fightcam height ground
```

Makes the camera follow the average last-grounded height of the selected players.

```text
/fightcam anchor avg
```

Makes the camera follow the average position of both selected players.

```text
/fightcam anchor <p1/p2>
```

Makes the camera follow the first or second selected player and automatically changes distance to fixed.

## Building

Check out the branch for the Minecraft version you want, then run:

```sh
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

## Notes

FightCam is client-side and does not require spectator mode, although spectator mode can be useful when watching fights.

FlashBack replay compatibility is currently experimental. The mod detects FlashBack, but it does not yet include dedicated FlashBack replay integration.

## Developer

Ported by Zellior.


