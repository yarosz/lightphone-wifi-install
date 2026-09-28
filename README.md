# lightphone-wifi-install

Install Tools on a Light Phone III over Wi-Fi, from your computer, with no cable and no adb. It uploads the
Tool's APK to the Tool Inbox of LightOS's own File Manager, and LightOS installs it from there.

This is unofficial and isn't affiliated with Light. Light documents a cable-free route of its own,
[Installing Tools Locally](https://github.com/lightphone/light-sdk/blob/main/docs/sideloading/README.md), whose
`Developer` section LightOS 582 doesn't show yet. This uses the part that does work today, the Tool Inbox, and a
LightOS update could change or remove it. Tested on a Light Phone III (TLP301) with LightOS 582.

## What you need

- A Light Phone III and a computer on the same Wi-Fi, with any VPN on the computer turned off.
- Python 3.9 or newer on the computer. macOS includes it; on Windows, install it from
  [python.org](https://www.python.org/downloads/). Nothing else: the installer uses only Python's standard
  library.
- A camera on the computer, to read the phone's QR code. The phone shows only the QR code, not its address, so
  without a camera scan the code with a phone's camera and paste the link it opens.

## Once, on the phone

1. Turn on developer mode for your phone on Light's [user dashboard](https://dashboard.thelightphone.com). It
   only makes Settings > Developer appear, for the next step; you can turn it off again afterwards.
2. In the phone's Settings > Developer, set Allowed tools to All tools. This is required: with No external,
   Light approved or Light signed, the phone still installs what you upload but hides it from the Tools list,
   unless Light signed it. It stays set when developer mode is off.
3. Open the Phone tool and dial `*7412369#`. That's what makes Settings show Debug, where the File Manager is:
   on the phone this was tested on, Debug never appeared until then. The code also opens a developer menu, where
   nothing needs changing for Wi-Fi install.

## Installing a Tool

1. Download [`lp3-install.py`](lp3-install.py) and run it: `python3 lp3-install.py` (on Windows,
   `py lp3-install.py`). It opens a page at `http://localhost:54450/`; leave the window open while you install.
2. Choose the Tool: drop its `.apk` file on the page, or paste a link to it. Add the Tool's published SHA-256 if
   it lists one.
3. Check what the page shows: package, version, permissions and SHA-256, and whether it's a Light Tool.
4. On the phone, open Settings > Debug > File Manager. Hold its QR code up to the computer's camera.
5. Click Install on the phone. The Tool appears in the phone's Tools list a few seconds later.

One scan lasts until you press Back in the File Manager. The phone's screen can go to sleep, and you can click
Install another to add more Tools on the same scan. The page shows whether it's still connected.

## If the phone refuses the upload

You may never see this. On the one phone this was tested on, the first uploads were refused with "Invalid path",
because the folder behind the Tool Inbox didn't exist yet. Reading LightOS showed that the folder is created when
LightOS downloads a Tool you add, so we added Weather and Authenticator with the (+) button at the bottom of the
Tools list, and uploads worked from then on. If you hit the same refusal, try adding a Tool you don't have yet;
Weather and Authenticator are the ones we tried.

## From a terminal

[`wifi-install.sh`](wifi-install.sh) does the same with curl, on macOS or Linux:

```
./wifi-install.sh -a https://example.com/some-tool.apk -h <sha256>
./wifi-install.sh -a some-tool.apk 'https://192-168-1-20.my.local-ip.co:54449/#<key>'
```

Without the File Manager's address it runs [`qr-scan.py`](qr-scan.py), which opens a camera page in the browser
and reads the QR code off the phone.

## Install links, for Tool authors

The page opens with your Tool filled in from a link like this, so owners only have to scan and click:

```
http://localhost:54450/?apk=<percent-encoded https link to your APK>&sha256=<its SHA-256>
```

The installer has to be running for the link to open. Publish the SHA-256 with each release, so the page can
check the download.

## Staying safe

LightOS installs whatever reaches the Tool Inbox without asking, whatever Allowed tools is set to, and with
All tools it lists and opens ordinary Android apps too, not just Light Tools. So:

- Only install APKs from people you trust. The page notes when an APK is an ordinary Android app rather than a
  Light Tool, but it can't tell a safe APK from a harmful one.
- Use the published SHA-256 when there is one. The page refuses an APK that doesn't match.
- Leave the File Manager with Back when you're done. Its address works for anyone on your Wi-Fi who has it,
  until you do.

## How it works

The phone's File Manager runs an HTTPS server on port 54449 while it's open. Its QR code holds
`https://<ip>.my.local-ip.co:54449/#<key>`. The key changes each time the File Manager opens, and the server
and key last until you press Back: screen-off, an hour of deep sleep and LightOS's Home gesture don't end it.
The installer:

1. sends the key as `Authorization: Bearer <key>` on every request;
2. `POST`s the APK's bytes as `application/octet-stream` to `/api/upload/Tool%20Inbox/<package>.apk`;
3. `POST`s an empty body to `/api/notify/Tool%20Inbox`, which starts LightOS's inbox scan;
4. polls `GET /api/files/Tool%20Inbox?page=0&size=100&sortBy=NAME&sortOrder=ASC` until the file is gone,
   because LightOS deletes each APK once it has installed it.

Quirks it works around:

- The Tool Inbox is backed by a folder that, in LightOS 582's code, only its third-party download creates
  (`LightOSThirdPartyApkDownloadWorker`). If the folder is missing, every upload fails with "Invalid path"; see
  [If the phone refuses the upload](#if-the-phone-refuses-the-upload).
- Listing an empty Tool Inbox returns HTTP 500, so a 500 there doesn't mean anything is broken.
- The server sends its certificate without the intermediate that signed it. Browsers and curl fetch the missing
  intermediate themselves; Python doesn't, so the installer fetches it from the certificate's CA Issuers address
  and still requires a chain to a root the computer trusts.
- The server drops TLS connections that don't offer ALPN, so the installer offers `http/1.1`.
- The server sends no CORS headers and answers the browser's preflight with 401, so a web page hosted elsewhere
  can't upload. That's why the installer is a program on your computer and not a website.

## Known limits

- Tested only on one LP3 with LightOS 582, and on macOS. Windows and Linux should work but haven't been tried.
- The installer can't see whether LightOS's install succeeded, only that it took the APK from the Tool Inbox.
- It doesn't uninstall. Remove a Tool on the phone as you'd remove any Tool.
- Chrome with secure DNS may fail to open the phone's own File Manager page; that doesn't affect this installer.

## Related

- [Light Phone Manager](https://github.com/greghare/light-phone-manager) tracks community Tools and installs
  them over USB with adb.
- [awesome-light](https://awesome-light.garado.dev) is a directory of community Tools and apps.

## License

MIT, see [LICENSE](LICENSE). The page loads [jsQR](https://github.com/cozmo/jsQR) 1.4.0 (Apache-2.0) from
jsDelivr, pinned with a subresource integrity hash.
