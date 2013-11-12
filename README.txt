Author: Tamino Hartmann

BUGS:
 - Rotation of 3D models is not correct: the z-Axis seems to be inverted, but I
   haven't been able to fix it. Probably trivial...
 - OpenGL Canvas does not align over OpenCV Camera view. This is due to the
   scaling the OpenGL canvas does, stretching the canvas to fill the whole screen
   where OpenCV only renders the camera partialy. Ideally we'd pin both canvases
   to the same size, but thanks to android layout xml I haven't managed to manage
   that. Alternatively rewrite the framework to draw to a single, shared canvas
   – might slow it down quite a bit, though.

TODO:
 - No color support for the basic .obj importer. Should be done so that vertice
   color doesn't have to be done by hand.
 - Marker rotation is not applied to 3D model. Since orientation is buggy anyway,
   we haven't started on this. However, the angle is there, it must simply be
   applied.

IMPROVEMENTS:
 - We could improve the flow of the program by allowing flags to control whether
   or not an action is done – ie allow a flag to set that orientation is not read
   or used in any way. This would also improve debugging when something isn't
   working, as it would allow to pinpoint the not-working-section-of-code.

BUGS is just that; bugs that need to be fixed for the framework to work perfectly.
TODO is features that should be done to improve the framework; note that applying
marker orientation should be first priority if exact rotation info is needed.
IMPROVEMENTS are just general things that could be done – note that the scope
might be pretty large. And no, I'm not very satisfied with my code – I think
there is vast room for improvements, to put it nicely. But at least the core stuff
works. And it was my first AR project, so not quite bad if I may say so myself.
