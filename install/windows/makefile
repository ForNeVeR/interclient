#Copyright (C) 1996, Borland International
#WHAT YOU CAN PASS IN:
#TARGETS (exclusive )
#	-DSERVER
#	-DLIBS
#	-DCLASSIC
#	-DCLIENT
#   BUILD OPTIONS
#	-DDEV	(debug dll/exe)
#	-DSDEV	(debug script)
#	-DVC	(use VC for dll/exe)
#	-DNMAKE	(using NMAKE)
#	-DPPC   (STUFF for PPC, sets PLATFORM to 60X)
#	-DX86   (default, sets PLATFORM to X86)
#   MEDIA OPTIONS
#	-DSINGLE (default, $(CUTTYPE) will be S 
#	-DMULTI (note: multi disk installs can still be used from a CD. you
#		 should either put every thing in the same directory, or create
#		 DISK[1-n] directories. Any product that ships on both media
#		 should only be built with MULTI. SINGLE is for products such
#		 as DELPHI which are never on floppy. All non-embeded 
#		 InterBase SKUs to date use MULTI. $(CUTTYPE will be M)
#	-DUNCOMPRESSED (install directly from run image) (NOT IMPLEMENTED)
#     Below only make sense for MULTI
#	-DDISKSIZE=n (where n is the size of a split file in KB. Default 1400)
#	-DDISK1SIZE=n (where n is size in KB to reserve on disk1. Default 660)
#	-DDISKLIST="1 2 3 .... n" (this is a list of the numbers for the disks.
#				   the default depends on the target type.
#				   It does not include ISEXPRESS) 	
# other options 
#	-DREGCFG (Install uses REGCFG.DLL) (nothing uses this right now, but
#					    there are some #ifdefs in the code)
#	-DPIPELINE (PIPELINE registration) 
#		Option directories:
#			DATA_ADD=P
#			CUT_ADD=P
#			SRC_ADD=P 
#	-DODBC	(INSTALL ODBC) 
#		Option directories:
#			DATA_ADD=O
#			CUT_ADD=O
#			SRC_ADD=O 
#	-DISEXPRESS (InstallShield Express deployment tool) 
#		Option directories:
#			DATA_ADD=I
#			CUT_ADD=I
#			SRC_ADD=I 
# The above will happen in the order shown, so if you make an install with
# -DODBC -DPIPELINE -DISEXPRES, option directory will be $(PLATFORM)POI
#	-DAPI (Enable API related stuff as an option) 
#	-DCLIENT_LICENSE (Allow client license. Should be on for CLIENT and
#			  SERVER, but not always LIBS)
#	-DSVC (stuff for service, should always be on for CLASSIC, and 
#	       for SERVER if superserver actually happens)
# NOTE -DSVC affects setup.ins, ibinst.dll and ibuninst.exe, so it should have
#       its own option directory. However there aren't currently service and
#	non service versions of the same install.
#
#	-DTOUCHTIME=<time> time stamp for files, format depondes on if you use
#	  Borland or MKS touch. Default is 0410 (4:10 am)
#	-DTOUCHDATE=<date> Date stamp for files. Default is today
# DIRECTORY MAP
#   Source files
#	MACRO: $(SRC)
# 	DEFAULT: SRC	
#   User generated files that are specific to a target.
#	MACRO: $(SRC_COMM_DIR)
#	$(SRC)\$(TARGET)	
#   User generated files that are specific to a target options.
#	MACRO: $(SRC_OPT_DIR)
#	$(SRC)\$(TARGET)\$(SRC_OPTS)
#   Installshield files, possibly modified (be sure to update these when you
#   update installshield.)
#	MACRO: $(ISFILES_DIR)
#	ISFILES	
#   Files to be compressed in main pack file
#   DATA should be delivered by DD but I haven't created the DDM tables yet.
#	MACRO: $(DATA_DIR)
#	DATA\$(TARGET)\$(DATA_ADD)
#   Built files for target. There should not be anything below here that isn't
#   copied or built from somewhere else. This allows you to make a completly 
#   clean build by doing an rm -r $(TARGET) then a MAKE TREE DISK (with the
#   required defines, of course.)
#	MACRO: $(TARGET_DIR)
#	$(TARGET)\$(CUT_ADD)	
#   Built DLL/EXE files and compiler garbage
#	$(TARGET_DIR)\BIN
#   Built DLL/EXE files and compiler garbage (DEBUG)
#	$(TARGET_DIR)\BIND
#   Built files that go uncompressed on the first disk.
#   These files differ depending on single/multi disk installs.
#	MACRO: $(SETUPBIN_DIR)
#	$(TARGET_DIR)\SETUPBIN.$(CUTTYPE)
#   Files that must be compressed in setup.lib.
#	MACRO: $(SETUPLIB_DIR)
#	$(TARGET_DIR)\SETUPLIB
#   In single disk installs, this is the final product.
#	$(TARGET_DIR)\CUT\CD
#   Disk images (multi disk only)
#	$(TARGET_DIR)\CUT\DISKn
#   Place to split DATA.Z (multi disk only)
#	$(TARGET_DIR)\PACK_TMP
#
# OTHER NOTES:
# 1)  The environment variable INSTRC should have the ISFILES/INCLUDE and SRC 
#  directories in it (eg SET INSTRC=c:\INSTDEV\ISFILES\INCLUDE;C:\INSTDEV\SRC)
#  The paths in INSTRC must be short.
# 2)  The packing list files files (SRC/$(TARGET)/SETUP.LST, MDSETUP.LST, etc) 
#  MUST contain hard coded paths, so if you change the pack targets in the
#  makefile, (eg PACKROOT=f:\MYPACK) then you must edit these files.
# 3) REGCFG.DLL is copied from the DATA directory to SETUPLIB to make sure we
#  use the same one as in the pack file.  (if -DREGCFG is passed in)
# 4) IBUNINST.EXE doesn't really belong in _SETUP.LIB, but it gets rebuilt
#  durring install development, so you don't want to repack the whole data.z
#  each time.
# 5) for both cd and multi disk installs, the InstallShield express files are
#  copied to the ISXPRESS directory. for an actual floppy cut, you should copy
#  the original installshield disk images to floppy. A CD using the MULTI disk
#  LIBS cut should have and ISXPRESS directory at the same level as the
#  DISKn directories. If you have all the LIBS stuff in one directory, you
#  should have something like this on the CD:
#	\INTRBASE (local interbase stuff goes here)
#	\ISXpress (installshield express stuff goes here)
#  To change the names or locations above you must also change the code.
# 6) If you make TREE, you must already have the install type directory 
#   eg CLASSIC or LIBS existing, or have Command prompt extenstions enabled
#   so that md will create intermediate directories (if you say
# md dir1\dir2\dir3 and dir1 does not exist, they will all be created)
#

#TO DO
# 1)   include a config.mak so you don't need a bunch of command line
#  parameters if you are only developing one kind of target
# 2)   Create some targets for just updating parts of the install	
# 3)   Intergrate DATA trees with DDM

#TARGET DEFINES: 
!ifdef INTERSERVER
TARGET=		InterServer
!endif

!if "$(TARGET)"=="InterServer"
TARGET_DEFINE=	-DINTERSERVER
DISKLIST=1 2
BBOARD_BASE= BBRD
!message SERVER BUILD
!endif

!ifndef TARGET_DEFINE
!error TARGET TYPE MUST BE: INTERSERVER
!endif

!ifdef PPC
PLATFORM=60X
!else
PLATFORM=X86
!endif

!if "$(PLATFORM)"=="60X"
!message PPC BUILD
SCRIPT_DEFINES=	$(SCRIPT_DEFINES) -DIB_PPC
!endif

!if "$(PLATFORM)"=="X86"
SCRIPT_DEFINES=	$(SCRIPT_DEFINES) -DIB_X86
!endif

DATA_ADD=$(DATA_ADD)$(PLATFORM)
CUT_ADD=$(CUT_ADD)$(PLATFORM)
SRC_ADD=$(SRC_ADD)$(PLATFORM)

!ifdef JDBCTEST
SCRIPT_DEFINES=	$(SCRIPT_DEFINES) -DJDBCTEST
!endif

!ifdef XTRA_ICONS
SCRIPT_DEFINES=	$(SCRIPT_DEFINES) -DXTRA_ICONS
!endif

!ifdef MULTI
!message MULTI DISK
CUTTYPE=	M
!ifndef DISKSIZE
!message DISKSIZE not defined: defaulting to 1400
DISKSIZE=	1400
!endif
!ifndef DISK1SIZE
!message DISK1SIZE not defined: defaulting to 660
DISK1SIZE=	660
!endif
!ifndef DISKLIST
DISKLIST=	1 2 3 4 5
!message DISKLIST not defined: defaulting to 1 2 3 4 5
!endif
!endif

!ifdef SINGLE
!message SINGLE DISK
CUTTYPE=	S
!endif

!ifndef CUTTYPE
!message CUTTYPE not defined: defaulting to SINGLE
SINGLE=nothing in particular
CUTTYPE=	S
!endif

#misc release options
!ifndef TOUCHDATE
TOUCHDATE=-d07/18/00
!endif
!ifndef TOUCHTIME
TOUCHTIME=$(TOUCHDATE) -t13:00
!endif

!ifndef SRC
SRC=		SRC
!message USING DEFAULT SRC: $(SRC)
!endif

TARGET_DIR=	$(TARGET)\$(CUT_ADD)
DATA_DIR=	DATA\$(TARGET)\$(DATA_ADD)
CUT_DIR=	$(TARGET_DIR)\CUT
SRC_OPT_DIR=	$(SRC)\$(TARGET)\$(SRC_ADD)
SRC_COMM_DIR=	$(SRC)\$(TARGET)\$(SRC_ADD)
BBOARD_DIR=	SRC\BBOARDS

#dirs that are different for SINGLE and MULTI disk installs.
!ifdef MULTI
PACK_DIR=	$(TARGET_DIR)\PACK_TMP
DISK1_DIR=	$(CUT_DIR)\DISK1
!else
PACK_DIR=	$(CUT_DIR)\CD
DISK1_DIR=	$(CUT_DIR)\CD
!endif

!ifdef DEV
#debug output directories
OBJ_DIR=	$(TARGET_DIR)\BIND
EXE_DIR=	$(TARGET_DIR)\BIND
DLL_DIR=	$(TARGET_DIR)\BIND
RES_DIR=	$(TARGET_DIR)\BIND
!else
#non-debug output directories
OBJ_DIR=	$(TARGET_DIR)\BIN
EXE_DIR=	$(TARGET_DIR)\BIN
DLL_DIR=	$(TARGET_DIR)\BIN
RES_DIR=	$(TARGET_DIR)\BIN
!endif

#source directories
C_DIR=		$(SRC)
RC_DIR=		$(SRC)
DEF_DIR=	$(SRC)
RUL_DIR=	$(SRC)

#installshield redist directory
ISFILE_DIR=	ISFILES

#dir for built files that go uncompressed on disk1
SETUPBIN_DIR=	$(TARGET_DIR)\SETUPBIN.$(CUTTYPE)

#dir for files that go in _SETUP.LIB
SETUPLIB_DIR=	$(TARGET_DIR)\SETUPLIB

#dir for various bitmaps that go into setup.lib
BMP_DIR=	$(SRC_COMM_DIR)

#FILES
#file macros
#MAKEFILE	= makefile
SETUP_RUL	= $(RUL_DIR)\SETUP.RUL

!ifdef X86
ISENG		= _INST32I.EX_
!endif
!ifdef PPC
ISENG		= _INST32P.EX_
!endif

SETUP_INS	= $(SETUPBIN_DIR)\SETUP.INS
SETUP_PKG	= $(SETUPBIN_DIR)\SETUP.PKG
SETUP_INI	= $(ISFILE_DIR)\SETUP.INI
SETUP_BMP	= $(BBOARD_DIR)\SETUP.BMP
SETUP_LIB	= $(DISK1_DIR)\_setup.lib
PACKFILE	= $(PACK_DIR)\data.z
PACKBASE	= data

#files that are different for script debugging
!ifdef SDEV
SETUP_DBG	= $(SETUPBIN_DIR)\SETUP.DBG
ISDBGN_DLL	= $(ISFILE_DIR)\ISDBGN.DLL
!endif

#files that are different for single (CDROM) vs. multi disk installs
!ifdef SINGLE
SETUP_LST=	$(SRC_OPT_DIR)\SETUP.LST
!endif

!ifdef MULTI
SETUP_LST=	$(SRC_OPT_DIR)\MDSETUP.LST
!endif

!ifdef INTERSERVER
README_TXT=	$(DATA_DIR)\docs\readmes\Readme.txt
RELNOTES_TXT=	$(DATA_DIR)\docs\readmes\Relnotes.txt
JBUILDER_NOTES_TXT= $(DATA_DIR)\docs\readmes\JBuilderNotes.txt
INSTALL_TXT=	$(DATA_DIR)\docs\readmes\Install.txt
LICENSE_TXT=    $(DATA_DIR)\docs\readmes\License.txt
!endif

#file lists
BMP_FILES=	$(BBOARD_DIR)\COPYRT.BMP $(BBOARD_DIR)\TITLE.BMP\
		$(BBOARD_DIR)\$(BBOARD_BASE)?.BMP

#dll/exe files that we build for use  in the install
SUPPORT_PROGS=  

#files that must be on disk1, uncompressed (except pack/split files)
DISK1_UNCOMP=	$(ISFILE_DIR)\$(ISENG) $(ISFILE_DIR)\SETUP.EXE\
		$(ISFILE_DIR)\_ISDEL.EXE $(ISFILE_DIR)\_SETUP.DLL\
		$(INSTALL_TXT) $(SETUP_INS) $(SETUP_INI) $(SETUP_PKG)\
		$(SETUP_DBG) $(ISDBGN_DLL) $(SETUP_BMP)

#files that must be packed in _SETUP.LIB (bootstrap pack file)
DISK1_COMP=	$(SUPPORT_PROGS) $(BMP_FILES) $(README_TXT)\
		$(ISFILE_DIR)\_ISRES.DLL $(ISFILE_DIR)\CTL3D32.DLL\
		$(ISFILE_DIR)\UNINST.EXE $(LICENSE_TXT) $(SUPPORT_PROGS)\
                $(RELNOTES_TXT) $(JBUILDER_NOTES_TXT) $(INSTALL_TXT) $(ISFILE_DIR)\MSVCRT40.DLL

SCRIPT_SOURCES= $(RUL_DIR)\setup.rul

#common commands
MOVE=		move
COPY=		COPY
DEL=		DEL /Q
DELTEREE=	deltree /Y
MD=		MD
ECHO=		echo
TOUCH=		touch
COMPILE=	compile
ICOMP=		icomp
PACKLIST=	packlist
SPLIT=		split

!ifdef MULTI
SPLIT_FLAGS=	-f$(DISKSIZE) -d1@$(DISK1SIZE) -c
SCRIPT_DEFINES=	$(SCRIPT_DEFINES) -DIB_MULTI
!endif

#flags that are different for script debuging
!ifdef SDEV
#installshield compiler debug flags
COMPILE_DEBUG_FLAGS=	-g
!endif

#installshield compiler flags
SCRIPT_DEFINES=	$(TARGET_DEFINE) $(SCRIPT_DEFINES)
COMPILE_FLAGS=	-W3 -S $(COMPILE_DEBUG_FLAGS)

#installshield script
$(SETUP_INS): $(SCRIPT_SOURCES) 
	$(COMPILE) $(COMPILE_FLAGS) $(SCRIPT_DEFINES) $(SETUP_RUL)
#because compile isn't smart about where it's output goes
#use copy since move affects time stamp
	$(COPY) $(RUL_DIR)\SETUP.INS $(SETUP_INS)
	$(DEL) $(RUL_DIR)\SETUP.INS

!ifdef SDEV
$(SETUP_DBG): $(SETUP_INS)
	$(COPY) $(RUL_DIR)\setup.DBG $(SETUP_DBG)
!endif

$(SETUP_PKG): $(SETUP_LST) $(PACKFILE)
	$(PACKLIST) $(SETUP_LST) $(PACKFILE)
	$(COPY) setup.pkg $(SETUP_PKG)
	$(DEL) setup.pkg

$(PACKFILE):
	$(ICOMP) -i $(DATA_DIR)\*.* $(PACKFILE)

#pack the setuplib dir
$(SETUP_LIB): DISK1_COMP_FILES
	$(ICOMP) $(SETUPLIB_DIR)\*.* $(SETUP_LIB)


#utility targets
#prepare the setuplib directory for packing
DISK1_COMP_FILES:  $(DISK1_COMP)
	$(DEL) $(SETUPLIB_DIR)\*.*
!ifdef NMAKE
	!$(COPY) $** $(SETUPLIB_DIR)
!else
	&$(COPY) $** $(SETUPLIB_DIR)
!endif
#this should probably not depend on *.*
	$(TOUCH) $(TOUCHTIME) -s $(SETUPLIB_DIR)\*.*

#make a single disk install
DISK1_UNCOMP_FILES: $(DISK1_UNCOMP) 
!ifdef NMAKE
	!$(COPY) $** $(DISK1_DIR)
!else
	&$(COPY) $** $(DISK1_DIR)
!endif

!ifdef MULTI
ISEXPRESS: 
	$(COPY) $(ISEXPRESS_DIR)\DISK1\*.* $(ISEXPRESSDISK)
	$(COPY) $(ISEXPRESS_DIR)\DISK2\*.* $(ISEXPRESSDISK)
	$(COPY) $(ISEXPRESS_DIR)\DISK3\*.* $(ISEXPRESSDISK)
!else
ISEXPRESS: $(ISEXPRESS_FILES)
!ifdef NMAKE
	!$(COPY) $** $(ISEXPRESSDISK)
!else
	&$(COPY) $** $(ISEXPRESSDISK)
!endif
!endif

#targets that you might actually want to build
#note this assumes you run [n]make from a command window started with
#cmd /X
TREE:
	-$(MD)	$(TARGET_DIR)
	-$(MD)	$(DLL_DIR)
	-$(MD)	$(SETUPBIN_DIR)
	-$(MD)	$(SETUPLIB_DIR)
	-$(MD)	$(CUT_DIR)
!ifdef MULTI
	-$(MD)	$(PACK_DIR)
	-for %x in ($(DISKLIST)) do $(MD) $(CUT_DIR)\DISK%x
!ifdef ISEXPRESS
	-$(MD)	$(ISEXPRESSDISK)
!endif #ISEXPRESS
!else #MULTI
	-$(MD)	$(DISK1_DIR)
!ifdef ISEXPRESS
	-$(MD)	$(ISEXPRESSDISK)
!endif #ISEXPRESS	
!endif #MULTI

#Copy files and pack _setup.lib
setup_lib: $(SETUP_LIB)

#Compile and copy script file
setup_ins: $(SETUP_INS) $(SETUP_DBG)
!ifdef NMAKE
	!$(COPY) $** $(DISK1_DIR)
!else
	&$(COPY) $** $(DISK1_DIR)
!endif

#rules to build complete install.
#DATA.Z will only be created if it does not exist
!ifdef SINGLE
CLEAN:
	$(DEL) $(CUT_DIR)\CD
!ifdef ISEXPRESS
	$(DEL) $(ISEXPRESSDISK)
!endif
	$(DEL) $(SETUPLIB_DIR)
	$(DEL) $(SETUPBIN_DIR)

#this should probably not depend on *.*
TOUCH_FILES:
	$(TOUCH) $(TOUCHTIME) $(CUT_DIR)\CD\*.*

DISK: DISK1_UNCOMP_FILES $(SETUP_LIB) TOUCH_FILES $(ISEXPRESS_TARGET)

!endif

!ifdef MULTI
CLEAN:
	$(DEL) $(CUT_DIR)\DISK?
!ifdef ISEXPRESS
	$(DEL) $(ISEXPRESSDISK)
!endif
	$(DEL) $(SETUPLIB_DIR)
	$(DEL) $(SETUPBIN_DIR)
	$(DEL) $(PACK_DIR)

SPLIT_FILES:
	-for %x in ($(DISKLIST)) do $(DEL) $(CUT_DIR)\DISK%x\$(PACKBASE).%x 
	$(SPLIT) $(PACKFILE) $(PACK_DIR) $(SPLIT_FLAGS)
	for %x in ($(DISKLIST)) do $(MOVE) $(PACK_DIR)\$(PACKBASE).%x $(CUT_DIR)\DISK%x
	for %x in ($(DISKLIST)) do $(ECHO) %x > $(CUT_DIR)\DISK%x\disk%x.id

TOUCH_FILES:
	for %x in ($(DISKLIST)) do $(TOUCH) -t$(TOUCHTIME) $(CUT_DIR)\DISK%x\*.*
	$(TOUCH) -s -w- -t$(TOUCHTIME) DATA\$(TARGET)\$(DATA_ADD)\*

#note that touch_files comes before ISEXPRESS, so these will not get 
#touched 
DISK: DISK1_UNCOMP_FILES $(SETUP_LIB) SPLIT_FILES TOUCH_FILES\
	 $(ISEXPRESS_TARGET)

!endif
CLEAN_ALL:
	$(DELTREE) $(TARGET_DIR)

