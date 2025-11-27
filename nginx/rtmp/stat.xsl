<?xml version="1.0" encoding="utf-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" />

<xsl:template match="/">
    <html>
        <head>
            <title>RTMP 流媒体统计</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
                h1 { color: #333; }
                table { border-collapse: collapse; width: 100%; margin-top: 20px; background: white; }
                th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                th { background-color: #4CAF50; color: white; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .online { color: #4CAF50; font-weight: bold; }
                .offline { color: #f44336; font-weight: bold; }
                .section { margin-top: 30px; padding: 20px; background: white; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
            </style>
        </head>
        <body>
            <h1>络绎视频流服务器 - 实时统计</h1>
            
            <div class="section">
                <h2>服务器信息</h2>
                <table>
                    <tr><th>运行时间</th><td><xsl:value-of select="rtmp/uptime"/> 秒</td></tr>
                    <tr><th>接受连接数</th><td><xsl:value-of select="rtmp/naccepted"/></td></tr>
                    <tr><th>输入带宽</th><td><xsl:value-of select="format-number(rtmp/bw_in div 1024, '#.##')"/> KB/s</td></tr>
                    <tr><th>输出带宽</th><td><xsl:value-of select="format-number(rtmp/bw_out div 1024, '#.##')"/> KB/s</td></tr>
                </table>
            </div>

            <xsl:apply-templates select="rtmp/server"/>
        </body>
    </html>
</xsl:template>

<xsl:template match="server">
    <div class="section">
        <h2>RTMP 应用</h2>
        <xsl:apply-templates select="application"/>
    </div>
</xsl:template>

<xsl:template match="application">
    <h3>应用: <xsl:value-of select="name"/></h3>
    <table>
        <tr>
            <th>流名称</th>
            <th>状态</th>
            <th>客户端数</th>
            <th>输入带宽</th>
            <th>输出带宽</th>
            <th>视频编码</th>
            <th>音频编码</th>
        </tr>
        <xsl:apply-templates select="live/stream"/>
    </table>
</xsl:template>

<xsl:template match="stream">
    <tr>
        <td><xsl:value-of select="name"/></td>
        <td class="online">在线</td>
        <td><xsl:value-of select="nclients"/></td>
        <td><xsl:value-of select="format-number(bw_in div 1024, '#.##')"/> KB/s</td>
        <td><xsl:value-of select="format-number(bw_out div 1024, '#.##')"/> KB/s</td>
        <td><xsl:value-of select="meta/video/codec"/> <xsl:value-of select="meta/video/width"/>x<xsl:value-of select="meta/video/height"/></td>
        <td><xsl:value-of select="meta/audio/codec"/> <xsl:value-of select="meta/audio/sample_rate"/>Hz</td>
    </tr>
</xsl:template>

</xsl:stylesheet>
