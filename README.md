# Project status

![Build Status](https://github.com/marcin-chwedczuk/rfid/actions/workflows/basic-ci.yaml/badge.svg)

# How to run?

If you are a MacOS user, and the application reports error (check the logs):
```
Caused by: java.io.IOException: No PC/SC library found on this system
```
please make sure that the following line is uncommented in `pom.xml`:
```xml
<options>
    <!-- Workaround for BUG: https://stackoverflow.com/a/65062759/1779504 -->
    <option>-Dsun.security.smartcardio.library=/System/Library/Frameworks/PCSC.framework/PCSC</option>
</options>
```
For all other operating systems, the above line must be commented out.

To start application simply run:
```
$ ./mvnw javafx:run
```

I tested the app with both JDK 11 and JDK 14.

# Setup ACR122U Reader

## MacOS
On MacOS everything should work out of the box, just plug your ACR122U
and wait till the status LED turns red.

## GNU/Linux
Quick setup for GNU/Linux, using old `pcsc-lite` library:
```
sudo apt install pcsc-tools pcscd
```

Then please create `/etc/modprobe.d/blacklist.conf` file (if it not exists) and
add the following lines:
```
install nfc /bin/false
install pn533 /bin/false
install pn533_usb /bin/false
```
Warning: this will break nfc-tools(`libnfc`) if you have it installed.

Then remove these modules:
```
sudo rmmod pn533_usb
sudo rmmod pn533
sudo rmmod nfc
```
and restart `pcscd` service:
```
sudo service pcscd restart
```
Finally, connect ACR122U and run `pcsc_scan`. 
The reader should be detected now.

Above process is based on 
[this tutorial](https://oneguyoneblog.com/2016/11/02/acr122u-nfc-usb-reader-linux-mint/)
although I did not install the ACR drivers.

## Windows
The last but not... whatever, I do not use Windows and thus I cannot
test my app there, but everything should just work.

# Sending commands to the reader when card is not present

You must first allow sending so called "PC/SC Escape Command"
before you can use this function.

To do this on Linux, please edit `/etc/libccid_Info.plist` (make a backup first!).
You should change `ifdDriverOptions` to `0x0001`:
```
<key>ifdDriverOptions</key>
<string>0x0001</string>

<!-- Possible values for ifdDriverOptions
0x01: DRIVER_OPTION_CCID_EXCHANGE_AUTHORIZED
        the CCID Exchange command is allowed. You can use it through
        SCardControl(hCard, IOCTL_SMARTCARD_VENDOR_IFD_EXCHANGE, ...)
```

The same procedure can be followed on macOS before BigSur,
see [this GitHub comment](https://github.com/pokusew/nfc-pcsc/issues/13#issuecomment-302482621)
for detail. I was not able to change this value on macOS BigSur though.

See `docs/API-ACR122U-2.04.pdf` document in this repository to find out how you
can do this on Windows (pages 38 - 40).

