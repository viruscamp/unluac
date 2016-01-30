local hour, apm
hour = hour and tonum(hour,0,apm and 12 or 24,'hour') or 12
--hour = hour and tonum() or 12
