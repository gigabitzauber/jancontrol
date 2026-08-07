# jancontrol 0.6.0

* [Home](https://github.com/gigabitzauber/jancontrol)
* [Consider supporting if you find this useful](https://ko-fi.com/gigabitzauber)

# Notable Changes

* Introduced n-PieceWiseInterpolator.
    * Calculated RPM are now always a multiple of n.
* Added the -w switch.
    * Support for reloading config file without having to restart.
* Introduced the downSkip hysteresis.
    * Decreasing RPMs can now be throttled by skipping a couple of iterations.
