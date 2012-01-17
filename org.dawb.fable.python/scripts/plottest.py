print "fable - python 1d plot test script"
import numpy
import math
a=numpy.array([(i-50)/2.0 for i in range(100)])
b=numpy.zeros(100)
for i in range(100):
    if math.fabs(a[i]) < 0.001:
        a[i] = 0.001
    b[i] = math.sin(a[i])/a[i]

plot.1d(b)
c=b*b
plot.1d(c)
