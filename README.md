
# How to run?

If you are a MacOS user, and application reports error (check logs):
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
$ mvn javafx:run
```

I tested the app with both JDK 11 and JDK 14.

# Setup ACR122U Reader

On MacOS everything should work out of the box, just plug your ACR122U
and wait till the LED turns red.

Quick setup for GNU/Linux, using old `pcsc-lite` library:
```
sudo apt install pcsc-tools pcscd
```

Then please add `/etc/modprobe.d/blacklist.conf` file with the lines:
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
and restart `pcscd`:
```
sudo service pcscd restart
```
Finally, connect ACR122U and run `pcsc_scan`. 
The reader should be detected now.

Above process is based on 
[this tutorial](https://oneguyoneblog.com/2016/11/02/acr122u-nfc-usb-reader-linux-mint/)
although I did not install the ACR drivers.

The last but not... whatever, I did not use Windows but everything should
work out of the box there too.

