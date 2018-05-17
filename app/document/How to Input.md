<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="file:///github-markdown.css" ref="stylesheet" type="text/css">
    <style>
        .markdown-body {
            box-sizing: border-box;
            margin: 0 auto;
            padding: 15px;
        }
    </style>
</head>
<body>
<p>You can input with tap or flick.<br />
If you want to check keymap, see <a href="file:///android-asset/keymap.md">Here</a>.</p>
<p><a id="1"></a></p>
<h2>Multi phase flick</h2>
<p>On flick input, QuickFlick calculates distance level in response to distance from first tap point, and detects internal keycode to input in accordance with distance level. <br />
(ex. small flick --&gt; upper char(like <code>A</code>), large flick --&gt; lower char(like <code>a</code>))</p>
<h3>Flick indicator</h3>
<p>You can check flicking state(direction, distance level) in <em>Flick Indicator</em> located at top of keyboard.<br />
Indicator shows direction with hue, distance level with transparent of color.  </p>
<h3>Change flick sensitivity</h3>
<p>You can change flick sensitivity in <a href="file:///android-asset/Settings.md#1">Settings &gt; Horizontal/Vertical Flick Threshold</a>.</p>
<p><a id="2"></a></p>
<h2>Canceling with multi tap</h2>
<p>You can control input with multi tap.<br />
QuickFlick has now two functions to control input.
<em> <strong>Flick reseter</strong>: Reset flicking state with multi tap.
</em> <strong>Input canceler</strong>: Cancel input with twice tap.</p>
<p>These functions are able to toggle on-off in <a href="file:///android-asset/Settings.md#2">Settings &gt; Multi tap settings</a></p>
</body>
</html>