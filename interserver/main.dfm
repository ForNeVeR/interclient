object dlgMain: TdlgMain
  Left = 336
  Top = 213
  BorderIcons = [biSystemMenu, biHelp]
  BorderStyle = bsSingle
  Caption = 'InterServer Configuration Utility'
  ClientHeight = 377
  ClientWidth = 336
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clBlack
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  KeyPreview = True
  OldCreateOrder = True
  Position = poScreenCenter
  ShowHint = True
  OnShow = SetupDialog
  PixelsPerInch = 96
  TextHeight = 13
  object TImage
    Left = 257
    Top = 82
    Width = 16
    Height = 16
    AutoSize = True
    Picture.Data = {
      07544269746D6170F6000000424DF60000000000000076000000280000001000
      000010000000010004000000000080000000C40E0000C40E0000100000000000
      0000000000000000800000800000008080008000000080008000808000008080
      8000C0C0C0000000FF0000FF000000FFFF00FF000000FF00FF00FFFF0000FFFF
      FF00333333FFFFF333333333F777773FF3333337733FFF773F3333733F777FFF
      73F337F37733377FF7F3373733777337F73F7F3737333737FF7F7F737F333373
      7F7F7F737F3333737F7F7F737FF33373737F73F737FFF737F37337F733777337
      37F3373F7733377337333373FF77733F7333333773FFFF773333333337777733
      3333}
  end
  object PageControl1: TPageControl
    Left = 7
    Top = 8
    Width = 321
    Height = 337
    ActivePage = GeneralPage
    TabOrder = 0
    object GeneralPage: TTabSheet
      Caption = 'General'
      object Image2: TImage
        Left = 13
        Top = 13
        Width = 32
        Height = 32
        AutoSize = True
        Picture.Data = {
          055449636F6E0000010002002020100000000000E80200002600000010101000
          00000000280100000E0300002800000020000000400000000100040000000000
          8002000000000000000000000000000000000000000000000000800000800000
          00808000800000008000800080800000C0C0C000808080000000FF0000FF0000
          00FFFF00FF000000FF00FF00FFFF0000FFFFFF00000000000000000000000000
          0000000000000000000000000008888888888880008080808080800000800000
          00000000080808080808080008008F7878787870808080808080808000088F78
          78787870080808080808080000888F7878787870008080808080800007888F78
          78787870000808080808000007888F7878787870000000000000000007888F78
          78787870000000000000000007888F7878787870000000000000000007888F78
          78787870000000000000000007888F7878787870000000000000000007888F78
          78787870391000000000000007888F7878787870991000000EC0000007888F78
          7878787031900000EC4C000007888F78787878700100004EC4C4C00007888F78
          78787870090004EC4C4CCC0007888F787878787000004EC4C4CCC4C007888F78
          787878700004EC4C4CCC4C4C00888F787878787000EEE4C4CCC4C4C4C0888F78
          F8F8F870009B9E4CCC4C4C4CE0888F788888887000F9B9ECC4C4C4CEC0888F77
          7777777000F7FCCE4C4C4CEC07888F777778887000F0FCCCE4C4CEC000888F77
          777AAA700F00F7CCCE4CEC0CC0888F7777788870F0FF007CCCEEC00EC0F88F77
          77799970F00F0007CCEC000C000F8F7777777770F00F000077E0C0C40000FFFF
          FFFFFFF0F0FF00000004CC400000000000000000F0F000000000440000000000
          0000000000F00000000000000000000000000000FFFFE001FFFFC000D5578000
          AAAB000055550000AAAB0000D5570000EAAF0000FFFF0000FFFF0000FFFF0000
          FFFF0000FFFF00001F0F00001E0700001C030000B8010000B0000000E0000000
          C0000000800000008000000080000000C0000000C0000000B000000040020000
          6C0600006E0460004F00F0005FE1FFFFDFFFFFFF280000001000000020000000
          0100040000000000C00000000000000000000000000000000000000000000000
          000080000080000000808000800000008000800080800000C0C0C00080808000
          0000FF0000FF000000FFFF00FF000000FF00FF00FFFF0000FFFFFF0000080800
          0000000000808080F878700000080880F878700000000080F878700000000080
          F878700000000080F878700000000080F8F8F00000000088F888800000001080
          077770000000900E40797000000000EC440FF00000000EC4CC000000000007EC
          C00000000000F07E080000000000F7000000000000000F0000000000EA030000
          D4030000E8030000FC030000FC030000FC030000FC030000FC030000F4030000
          F4030000F8030000F0030000F01F0000F01F0000F03F0000FBFF0000}
      end
      object Label1: TLabel
        Left = 20
        Top = 70
        Width = 72
        Height = 13
        Caption = 'Server Version:'
      end
      object Label3: TLabel
        Left = 30
        Top = 205
        Width = 71
        Height = 13
        Caption = 'Server &Startup:'
        FocusControl = ServerStartup
      end
      object Label4: TLabel
        Left = 20
        Top = 92
        Width = 67
        Height = 13
        Caption = 'Server Status:'
      end
      object Label5: TLabel
        Left = 61
        Top = 16
        Width = 208
        Height = 13
        Caption = 'Startup configuration options for InterServer.'
        WordWrap = True
      end
      object Bevel1: TBevel
        Left = 21
        Top = 53
        Width = 281
        Height = 17
        Shape = bsTopLine
      end
      object Label6: TLabel
        Left = 34
        Top = 240
        Width = 67
        Height = 13
        Caption = 'Startup &Mode:'
        FocusControl = StartupMode
      end
      object Bevel2: TBevel
        Left = 18
        Top = 142
        Width = 281
        Height = 17
        Shape = bsTopLine
      end
      object Label11: TLabel
        Left = 20
        Top = 115
        Width = 86
        Height = 13
        Caption = 'Operating System:'
      end
      object Label2: TLabel
        Left = 26
        Top = 169
        Width = 71
        Height = 13
        Caption = 'Root &Directory:'
        FocusControl = AppPath
      end
      object ServerStartup: TComboBox
        Left = 105
        Top = 201
        Width = 166
        Height = 21
        HelpContext = 5000
        Style = csDropDownList
        ItemHeight = 13
        TabOrder = 0
        OnChange = ServerStartupClick
      end
      object StartupMode: TComboBox
        Left = 105
        Top = 235
        Width = 166
        Height = 21
        HelpContext = 5010
        Style = csDropDownList
        ItemHeight = 13
        TabOrder = 1
        OnChange = StartupModeChange
        Items.Strings = (
          'Manual Startup'
          'Windows Startup')
      end
      object txtStatus: TPanel
        Left = 112
        Top = 92
        Width = 187
        Height = 13
        HelpContext = 4070
        Alignment = taLeftJustify
        BevelOuter = bvNone
        Caption = 'txtStatus'
        TabOrder = 2
      end
      object txtVersion: TPanel
        Left = 113
        Top = 70
        Width = 187
        Height = 13
        HelpContext = 4060
        Alignment = taLeftJustify
        BevelOuter = bvNone
        Caption = 'txtVersion'
        TabOrder = 3
      end
      object txtOS: TPanel
        Left = 113
        Top = 115
        Width = 187
        Height = 13
        HelpContext = 4080
        Alignment = taLeftJustify
        BevelOuter = bvNone
        Caption = 'txtOS'
        TabOrder = 4
      end
      object AppPath: TEdit
        Left = 105
        Top = 168
        Width = 166
        Height = 21
        HelpContext = 4090
        TabOrder = 5
        OnChange = AppPathChange
      end
      object btnBrowse: TBitBtn
        Left = 274
        Top = 168
        Width = 23
        Height = 21
        HelpContext = 4090
        TabOrder = 6
        OnClick = btnBrowseClick
        Glyph.Data = {
          76010000424D7601000000000000760000002800000020000000100000000100
          04000000000000010000120B0000120B00001000000000000000000000000000
          800000800000008080008000000080008000808000007F7F7F00BFBFBF000000
          FF0000FF000000FFFF00FF000000FF00FF00FFFF0000FFFFFF00555555555555
          5555555555555555555555555555555555555555555555555555555555555555
          555555555555555555555555555555555555555FFFFFFFFFF555550000000000
          55555577777777775F55500B8B8B8B8B05555775F555555575F550F0B8B8B8B8
          B05557F75F555555575F50BF0B8B8B8B8B0557F575FFFFFFFF7F50FBF0000000
          000557F557777777777550BFBFBFBFB0555557F555555557F55550FBFBFBFBF0
          555557F555555FF7555550BFBFBF00055555575F555577755555550BFBF05555
          55555575FFF75555555555700007555555555557777555555555555555555555
          5555555555555555555555555555555555555555555555555555}
        NumGlyphs = 2
        Style = bsWin31
      end
    end
    object AdvancedPage: TTabSheet
      Caption = 'Advanced'
      object Image3: TImage
        Left = 13
        Top = 13
        Width = 32
        Height = 32
        AutoSize = True
        Picture.Data = {
          055449636F6E0000010002002020100000000000E80200002600000010101000
          00000000280100000E0300002800000020000000400000000100040000000000
          8002000000000000000000000000000000000000000000000000800000800000
          00808000800000008000800080800000C0C0C000808080000000FF0000FF0000
          00FFFF00FF000000FF00FF00FFFF0000FFFFFF00000000000000000000000000
          000000000000000000FF000000088888888888800080808080FF700000800000
          0000000008080808080FF70008008F7878787870808080008080FF0000088F78
          78787870080800FF0800F70000888F7878787870008080FF700F7F0008888F78
          787878700008080FF7F7F78000888F787878787000000000FF7F7F7808088F78
          78787870000000000000F7F780808F88787878700000000000000F7F78080780
          8878787000000000000000F7F780878880787870000000000000000F7F780780
          888878703910000000000000F7F7800000887870991000000EC000000F7F7777
          7088787031900000EC4C000000F7F7F7F70878700100004EC4C4C000070F7F00
          FF707870090004EC4C4CCC00070FF0880F70787000004EC4C4CCC4C0070F7078
          700078700004EC4C4CCC4C4C000FF7087878787000EEE4C4CCC4C4C4C080FF70
          F8F8F870009B9E4CCC4C4C4CE0880FF08888887000F9B9ECC4C4C4CEC0888007
          7777777000F7FCCE4C4C4CEC07888F777778887000F0FCCCE4C4CEC000888F77
          777AAA700F00F7CCCE4CEC0CC0888F7777788870F0FF007CCCEEC00EC0F88F77
          77799970F00F0007CCEC000C000F8F7777777770F00F000077E0C0C40000FFFF
          FFFFFFF0F0FF00000004CC400000000000000000F0F000000000440000000000
          0000000000F00000000000000000000000000000FFCFE001FF87C000D5038000
          AA81000054410000A8210000D0010000E8000000FE000000FF000000FFF00000
          FFF80000FFFC00001F0E00001E0700001C030000B8010000B0000000E0000000
          C0000000800000008000000080000000C0000000C0000000B000000040020000
          6C0600006E0460004F00F0005FE1FFFFDFFFFFFF280000001000000020000000
          0100040000000000C00000000000000000000000000000000000000000000000
          000080000080000000808000800000008000800080800000C0C0C00080808000
          0000FF0000FF000000FFFF00FF000000FF00FF00FFFF0000FFFFFF00FF800000
          000000000FF8000080F8787000FF800880F87870000FF80080F878700000FFFF
          00F8787000000FF0F0F8787000000F0700F8F8F0000000F070F8888000000000
          80077770000000000E40797000000000EC440FF00000000EC4CC000000000007
          ECC00000000000F07E080000000000F0000000000000000F000000000F800000
          0700000082000000C0000000E0000000F0000000F0000000F8000000FD000000
          FF000000FE000000FC000000FC070000FC070000FD0F0000FEFF0000}
      end
      object Label7: TLabel
        Left = 62
        Top = 16
        Width = 178
        Height = 13
        Caption = 'Service configurations for InterServer.'
        WordWrap = True
      end
      object Bevel4: TBevel
        Left = 21
        Top = 53
        Width = 281
        Height = 17
        Shape = bsTopLine
      end
      object Label9: TLabel
        Left = 23
        Top = 70
        Width = 94
        Height = 13
        Caption = 'Services supported:'
      end
      object Label10: TLabel
        Left = 23
        Top = 111
        Width = 75
        Height = 13
        Caption = 'Service Control:'
      end
      object Bevel3: TBevel
        Left = 20
        Top = 188
        Width = 281
        Height = 17
        Shape = bsTopLine
      end
      object warningTxt: TLabel
        Left = 70
        Top = 203
        Width = 230
        Height = 67
        AutoSize = False
        Caption = 
          'Removing the InterServer registry information from this machine ' +
          'will prevent any InterClient application from talking to the Int' +
          'erBase server.  This information can only be removed if InterSer' +
          'ver is not running.'
        WordWrap = True
      end
      object Image1: TImage
        Left = 19
        Top = 211
        Width = 32
        Height = 32
        AutoSize = True
        Picture.Data = {
          07544269746D617076020000424D760200000000000076000000280000002000
          000020000000010004000000000000020000CE0E0000C40E0000100000000000
          000000000000000080000080000000808000800000008000800080800000C0C0
          C000808080000000FF0000FF000000FFFF00FF000000FF00FF00FFFF0000FFFF
          FF00777778888888888888888888888888777777888888888888888888888888
          88877737777777777777777777777778888873BBBBBBBBBBBBBBBBBBBBBBBB70
          88883BBBBBBBBBBBBBBBBBBBBBBBBBB708883BBBBBBBBBBBBBBBBBBBBBBBBBBB
          08883BBBBBBBBBBBB7007BBBBBBBBBBB08873BBBBBBBBBBBB0000BBBBBBBBBB7
          088773BBBBBBBBBBB0000BBBBBBBBBB0887773BBBBBBBBBBB7007BBBBBBBBB70
          8877773BBBBBBBBBBBBBBBBBBBBBBB088777773BBBBBBBBBBB0BBBBBBBBBB708
          87777773BBBBBBBBB707BBBBBBBBB08877777773BBBBBBBBB303BBBBBBBB7088
          777777773BBBBBBBB000BBBBBBBB0887777777773BBBBBBB70007BBBBBB70887
          7777777773BBBBBB30003BBBBBB088777777777773BBBBBB00000BBBBB708877
          77777777773BBBBB00000BBBBB08877777777777773BBBBB00000BBBB7088777
          777777777773BBBB00000BBBB0887777777777777773BBBB00000BBB70887777
          7777777777773BBB70007BBB088777777777777777773BBBBBBBBBB708877777
          77777777777773BBBBBBBBB08877777777777777777773BBBBBBBB7088777777
          777777777777773BBBBBBB0887777777777777777777773BBBBBB70887777777
          7777777777777773BBBBB788777777777777777777777773BBBB778777777777
          77777777777777773BB777777777777777777777777777777333777777777777
          7777}
      end
      object Panel1: TPanel
        Left = 105
        Top = 92
        Width = 75
        Height = 65
        HelpContext = 5020
        BevelOuter = bvNone
        Color = clSilver
        TabOrder = 0
        object greenLight: TImage
          Left = 3
          Top = 43
          Width = 20
          Height = 20
          OnClick = greenLightClick
        end
        object txtStart: TLabel
          Left = 27
          Top = 45
          Width = 22
          Height = 13
          Caption = 'Start'
          OnClick = greenLightClick
        end
        object txtPause: TLabel
          Left = 27
          Top = 25
          Width = 30
          Height = 13
          Caption = 'Pause'
          OnClick = yellowLightClick
        end
        object yellowLight: TImage
          Left = 3
          Top = 23
          Width = 20
          Height = 20
          OnClick = yellowLightClick
        end
        object redLight: TImage
          Left = 3
          Top = 3
          Width = 20
          Height = 20
          OnClick = redLightClick
        end
        object txtStop: TLabel
          Left = 27
          Top = 3
          Width = 22
          Height = 13
          Caption = 'Stop'
          OnClick = redLightClick
        end
      end
      object txtServices: TPanel
        Left = 128
        Top = 70
        Width = 172
        Height = 13
        HelpContext = 6060
        Alignment = taLeftJustify
        BevelOuter = bvNone
        Caption = 'txtStatus'
        TabOrder = 1
      end
      object btnRemove: TButton
        Left = 227
        Top = 279
        Width = 75
        Height = 21
        HelpContext = 6030
        Caption = '&Remove'
        TabOrder = 2
        OnClick = btnRemoveClick
      end
    end
  end
  object btnOK: TButton
    Left = 165
    Top = 350
    Width = 75
    Height = 21
    HelpContext = 6000
    Caption = 'OK'
    Default = True
    TabOrder = 1
    OnClick = btnOKClick
  end
  object btnCancel: TButton
    Left = 254
    Top = 350
    Width = 75
    Height = 21
    HelpContext = 6010
    Caption = 'Cancel'
    TabOrder = 2
    OnClick = btnCancelClick
  end
  object openDialog: TOpenDialog
    Options = [ofHideReadOnly, ofNoChangeDir, ofPathMustExist, ofFileMustExist, ofShareAware, ofNoReadOnlyReturn]
    Left = 219
    Top = 65535
  end
end
