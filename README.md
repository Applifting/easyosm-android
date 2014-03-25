easyosm
=======

Easyosm is an easy to use Open Street Map library for android. We needed  offline maps for our app, and the obvious choice was osmdroid. Over the course of time, however, we made so many modifications to osmdroid to add the neccesary features that it became apparent that we need a whole new lib. Here, we start from scratch borrowing a few things here and there from osmdroid.

Easyosm will not be able to do everything osmdroid does. It'll include the things we deem essential to a map and little more. We will go the easy-to-extend way, enabling users to write the more complex things themselves.

Easyosm is not a replacement for osmdroid, it's an alternative. If you want a minimap overlay, a compass, zoom and arrow buttons and map rotation right away, use osmdroid.

The key improvements will be:
  * better map experiece (fluent zooming and overzooming, better looking tile loading, better gesture responses, ...)
  * better marker and overlay system
  * _much more_ ease of use for both simple drop-in use and full customization
  * hopefully full Google map copatbility
