set arg1=%1

set /p UserInput=enter something please?
echo you entered %UserInput%

echo waiting %arg1% seconds

ping 127.0.0.1 -n %arg1% > NUL

echo finished!
