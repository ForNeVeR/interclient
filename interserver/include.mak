#------------------------------ INCLUDE.MAK ------------------------------------#
#	This is the common makefile that is included in the component
#	makefiles.  This set of makefiles is designed to work with
#	Borland Make, and will use both Borland and Microsoft compilers.
#
#	There are three command-line defines flags that are used by the
#	makefiles:
#		-DDEV - build a dev build (default: production build)
#		-DBORLAND - use Borland C (default: Microsoft C)
#		-DCLIENT - build client side stuff (default: server)
#
#	Depending on the options passed in, the output files go in 
#	different directories.  This allows one source tree to
#	target DEV and PROD builds on multiple compilers without
#	clobbering eachother.
#
#	The last complexity is the CLIENT vs SERVER libraries.  If you need
#	to link a program, use one of the following link flags.
#		SHRLIB_LINK = client link library
#		SVRLIB_LINK = server link library
#
#	This is NOT tested with Borland C, if you want to build using Borland C,
#	you'll need to do a little work.
#
#-------------------------------------------------------------------------------#



#
# Turn on automatic dependency checking.  Only seems to work with Borland C.
#
.autodepend

# Unfortunately, MAKEFLAGS does not prepend a '-', and does not pick
# up command line defines, so we will check for the ones we care about.

!if "$(MAKEFLAGS)" != ""
     MAKEFLAGS = -$(MAKEFLAGS)
!endif

ROOT = ..

#
# Set the COMPILER variable, default to Microsoft C.
#
!if $d(BORLAND)
!undef BORLAND
COMPILER=BC
RSP_CONTINUE=+
!else
!if $d(DELPHI)
COMPILER=DP
RSP_CONTINUE=
!else
COMPILER=MS
RSP_CONTINUE=
!endif
!endif

#
# Set the Version variable, default to PROD_BUILD.
# 	Check for the -DCLIENT switch, and alter the .path.obj accordingly
#
!if $d(DEV)
!undef DEV
VERSION=        DEV
VERSION_FLAG=   -DDEV_BUILD
DEBUG_OBJECTS=  nodebug.obj
.path.obj=obj
MAKEFLAGS = $(MAKEFLAGS) -DDEV
!else
VERSION=        PROD
VERSION_FLAG=   -DPROD_BUILD
DEBUG_OBJECTS=  nodebug.obj
.path.obj=obj
MAKEFLAGS = $(MAKEFLAGS) -DPROD
!endif

!if !$d(BRC32)
BRC32=			brc32.exe
!endif
#
# Borland C
#	Setup the macros for the Borland C compiler.
#	
!if $(COMPILER) == BC
!message Borland C Compiler
CC=                     bcc32
LINK=                   tlink32
IMPLIB=                 echo tlib
VENDOR_CFLAGS=          -w- -g0 -4 -pc -N -a -WM -WD -n$(.path.obj)
O_OBJ_SWITCH=           -o
O_EXE_SWITCH=           -e
DLLENTRY=
CONLIBSDLL=             
ADVAPILIB=
MPRLIB=
WSOCKLIB=

WIN_NT_GDSSHR=          gds32_nt_bc4.dll
SHRLIB_LINK=            $(ROOT)\jrd\$(.path.obj)\gds32.lib
WIN_NT_GDSINTL=         gdsintl_nt_bc4.dll

!if $(VERSION) == PROD
VERSION_CFLAGS=         -DFLINTSTONE
LINK_OPTS=              -WM
!else
VERSION_CFLAGS=         -v
LINK_OPTS=              -WM
LD_OPTS=                -v
!endif
!endif

#
# Microsoft C
#	Setup the macros for the Microsoft C compiler.
#
!if $(COMPILER) == MS
!message Microsoft C Compiler
CC=                     cl
LINK=                   link -machine:i386
IMPLIB=                 lib -machine:i386
VENDOR_CFLAGS=          -W3 -G4 -Gd -ML -Fo$(.path.obj)\ -DWIN32_LEAN_AND_MEAN
#VENDOR_CPPFLAGS=	-W3 -GX -ML -YX -Fo$(.path.obj)\ -Fp$(.path.obj)\interserver.pch  
VENDOR_CPPFLAGS=	-W3 -GX -ML -Fo$(.path.obj)\ -DWIN32_LEAN_AND_MEAN -DFD_SETSIZE=256
O_OBJ_SWITCH=           -Fo
O_EXE_SWITCH=           -o
DLLENTRY=               @12
CONLIBSDLL=             msvcrt.lib kernel32.lib
ADVAPILIB=              advapi32.lib
MPRLIB=                 mpr.lib
WSOCKLIB=               wsock32.lib

SVR_STATIC_LIB=		eng32lib.lib
CLIENT_STATIC_LIB=	gds32lib.lib
WIN_NT_GDSSHR=          gds32_nt_ms.dll
SHRLIB_LINK=            $(ROOT)\jrd\$(.path.obj)\gds32_ms.lib
SVRLIB_LINK=		$(ROOT)\jrd\$(.path.obj)\$(SVR_STATIC_LIB)
WIN_NT_GDSINTL=         gdsintl_nt_ms.dll

!if $(VERSION) == PROD
# Microsoft C
VERSION_CFLAGS=		-O2 -DWIN95 -DNDEBUG
LINK_OPTS=
!else
VERSION_CFLAGS=         -Zi -FR -DWIN95
LINK_OPTS=              -debug:full -debugtype:cv 
!endif
!endif

#
# Delphi
#	Setup the macros for the Delphi compiler.
#
!if $(COMPILER) == DP
!message Delphi Command-line compilier
DCC=                    dcc32
LINK=
CONVERT=		convert
DCC_FLAGS=

!if $(VERSION) == PROD
# Delphi
DCC_FLAGS=
!else
VERSION_CFLAGS=         -GP -GD -W -D -Y+
!endif
!endif

CFLAGS= $(VERSION_CFLAGS) $(VENDOR_CFLAGS) -DNOMSG -D_X86_=1 -DWIN32 -DI386
CPPFLAGS= $(VERSION_CFLAGS) $(VENDOR_CPPFLAGS) -DWIN32 -DWIN_NT -DARCH_32 -D_WINDOWS

GDSSHR_LINK=            $(SHRLIB_LINK) $(CONLIBSDLL)
VERSION_RC=		version_95.rc
VERSION_RES=		version_95.res

.path.res=		$(.path.obj)
.rc.res:
	$(BRC32) -r -w32 -fo$@ $<

.SUFFIXES: .c .e
.e.c:
	$(EXPAND_DBNAME) $<
	$(GPRE) $(GPRE_FLAGS) $<
	$(COMPRESS_DBNAME) $<
	$(TOUCH) $*.c

.dtm.dfm:
    $(CONVERT) $<

.SUFFIXES: .bin .obj .cpp
.cpp.obj:
	$(CC) -c @&&?
	$(CPPFLAGS)
	$(VERSION_FLAG) $<
?

.c.bin:
	$(CC) -c $(PIC_FLAGS) $(VERSION_FLAG) $(O_OBJ_SWITCH)$*.bin $<


#------------------------- Misc Utilities --------------------------------

#------------------------- Directory macros ------------------------------

#------------------------- Target Names ----------------------------------

#------------------------- OS macro defines ------------------------------
SH=                     echo
RM=                     -del /Q
MV=                     move
TOUCH=                  touch
CP=                     copy
ECHO=                   echo
GPRE=                   $(IBSERVER)\bin\gpre.exe
ISQL=			$(IBSERVER)\bin\isql.exe
GSEC=			$(IBSERVER)\bin\gsec.exe
GBAK=			$(IBSERVER)\bin\gbak.exe
QUIET_ECHO=             @echo
CD=                     cd
CAT=                    cat

#------------------------- Maintenance Rules ------------------------------
clean::
	$(RM) $(.path.obj)\*.*
