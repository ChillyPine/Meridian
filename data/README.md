# Subscriber name colors

`name_colors.json` is downloaded by every Meridian client on startup and every
10 minutes after. Edit it on `main` and changes show up in-game within about
10–15 minutes (GitHub caches raw files for ~5 min). Run
`/md namecolors reload` to pull right away.

```json
{
  "players": {
    "TheirIGN": {
      "colors": ["#6274E7", "#133A94"],
      "bold": true,
      "italic": false,
      "underlined": false
    }
  }
}
```

- **key**: the player's in-game name. Capitalization must match exactly (`ChillyPine`, not `chillypine`). If they change their name, update the key here.
- **colors**: one or more `#RRGGBB` hex colors. One color gives a solid name; two or more blend into a gradient across the name.
- **bold / italic / underlined**: optional, default `false`.

If the JSON is invalid, clients keep the last list they loaded, so a typo won't wipe everyone's colors. Still, check it with a JSON validator before you push.
Developer colors are hard-coded in `NameGradients.kt` and work even when this file can't be reached.
