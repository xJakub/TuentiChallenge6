<?php

/**
    We can just use any existing Piet interpreter to parse the mirrored image.
    However, coding one from scratch is more exciting!
    
    Note: this is not a fully functional implementation, although enough for
    solving this problem.
*/

define('DP_RIGHT', 0);
define('DP_DOWN', 1);
define('DP_LEFT', 2);
define('DP_UP', 3);
define('CC_LEFT', 0);
define('CC_RIGHT', 1);

class Piet {
    public $stack = [];
    public $dp = 0;
    public $cc = 0;
    public $pixelBlock = [];
    public $blockRight = [];
    public $blockTop = [];
    public $blockLeft = [];
    public $blockBottom = [];
    public $blockSize = [];
    public $blockColor = [];
    public $colorsTable = [];
    public $x = 0;
    public $y = 0;
    public $toggled = false;
    public $attempts = 0;

    function __construct($imageFile) {
        $this->colorsTable = [
            ['#FFC0C0','#FFFFC0','#C0FFC0','#C0FFFF','#C0C0FF','#FFC0FF'],
            ['#FF0000','#FFFF00','#00FF00','#00FFFF','#0000FF','#FF00FF'],
            ['#C00000','#C0C000','#00C000','#00C0C0','#0000C0','#C000C0']
        ];

        $this->im = imagecreatefrompng($imageFile);
        $this->imageWidth = imagesx($this->im);
        $this->imageHeight = imagesy($this->im);
        $this->findBlocks();
        $this->loop();
    }

    function loop() {
        if ($this->attempts == 8) { return; }

        $currentBlock = $this->pixelBlock[$this->x][$this->y];

        // 1. The interpreter finds the edge of the current colour block which is furthest in the direction of the DP.
        $edgeX = $this->x;
        $edgeY = $this->y;

        if ($this->dp == DP_DOWN) { $edgeY =  $this->blockBottom[$currentBlock]; }
        else if ($this->dp == DP_UP) { $edgeY =  $this->blockTop[$currentBlock]; }
        else if ($this->dp == DP_LEFT) { $edgeX =  $this->blockLeft[$currentBlock]; }
        else if ($this->dp == DP_RIGHT) { $edgeX =  $this->blockRight[$currentBlock]; }

        // 2. The interpreter finds the codel of the current colour block on that edge which is furthest
        // to the CC's direction of the DP's direction of travel.
        $codelDirection = ($this->dp-1 + 2*$this->cc + 4) % 4;
        $newX = $edgeX;
        $newY = $edgeY;

        if ($codelDirection == DP_DOWN) {
            for ($y=0; $y<$this->imageHeight; $y++) {
                if ($this->pixelBlock[$newX][$y] == $currentBlock) { $newY = $y; }
            }
        }
        else if ($codelDirection == DP_UP) {
            for ($y=$this->imageHeight-1; $y>=0; $y--) {
                if ($this->pixelBlock[$newX][$y] == $currentBlock) { $newY = $y; }
            }
        }
        else if ($codelDirection == DP_RIGHT) {
            for ($x=0; $x<$this->imageWidth; $x++) {
                if ($this->pixelBlock[$x][$newY] == $currentBlock) { $newX = $x; }
            }
        }
        else if ($codelDirection == DP_LEFT) {
            for ($x=$this->imageWidth-1; $x>=0; $x--) {
                if ($this->pixelBlock[$x][$newY] == $currentBlock) { $newX = $x; }
            }
        }

        // 3. The interpreter travels from that codel into the colour block
        // containing the codel immediately in the direction of the DP.
        $newX2 = $newX + [1, 0, -1, 0][$this->dp];
        $newY2 = $newY + [0, 1, 0, -1][$this->dp];
        // echo "New coordinates: {$newX2}, {$newY2} (dp={$this->dp}, cc={$this->cc})\n";

        if ($newX2 < 0 || $newX2 >= $this->imageWidth
            || $newY2 < 0 || $newY2 >= $this->imageHeight
            || $this->blockColor[$this->pixelBlock[$newX2][$newY2]] == '#000000') {

            $this->attempts++;
            if (!$this->toggled) {
                $this->cc = 1 - $this->cc;
                $this->toggled = true;
            }
            else {
                $this->dp = ($this->dp + 1) % 4;
                $this->toggled = false;
            }
        }
        else {
            $this->attempts = 0;
            $this->toggled = false;

            $newBlock = $this->pixelBlock[$newX2][$newY2];

            if ($this->blockColor[$newBlock] != '#FFFFFF' &&
                $this->blockColor[$currentBlock] != '#FFFFFF') {

                $oldHue = $this->getBlockHue($currentBlock);
                $newHue = $this->getBlockHue($newBlock);
                $hueDiff = ($newHue - $oldHue + 6) % 6;

                $oldLightness = $this->getBlockLightness($currentBlock);
                $newLightness = $this->getBlockLightness($newBlock);
                $lightnessDiff = ($newLightness - $oldLightness + 3) % 3;

                $commands = [
                    ['nop', 'pushVal', 'pop'],
                    ['add', 'substract', 'multiply'],
                    ['divide', 'mod', 'not'],
                    ['greater', 'pointer', 'ccSwitch'],
                    ['duplicate', 'roll', 'inNumber'],
                    ['inChar', 'outNumber', 'outChar']
                ];

                $command = $commands[$hueDiff][$lightnessDiff];
                // echo "Command: {$command}\n";
                $this->$command();
            }

            $this->x = $newX2;
            $this->y = $newY2;
        }

        $this->loop();
    }

    function findBlocks() {
        $nextBlock = 1;

        for ($x=0; $x<$this->imageWidth; $x++) {
            for ($y=0; $y<$this->imageHeight; $y++) {
                if (!isset($this->pixelBlock[$x][$y])) {
                    $this->blockLeft[$nextBlock] = $x;
                    $this->blockRight[$nextBlock] = $x;
                    $this->blockTop[$nextBlock] = $y;
                    $this->blockBottom[$nextBlock] = $y;
                    $this->blockSize[$nextBlock] = 0;

                    $color = strtoupper(dechex(imagecolorat($this->im, $x, $y) & 0xFFFFFF));
                    $color = '#' . str_pad($color, 6, '0', STR_PAD_LEFT);
                    $this->blockColor[$nextBlock] = $color;

                    $this->foundBlock($nextBlock, $x, $y);

                    $nextBlock++;
                }
            }
        }

        $blocksCount = $nextBlock-1;
        // echo "{$blocksCount} blocks found\n";
    }

    function foundBlock($block, $x, $y) {
        $pixelColor = imagecolorat($this->im, $x, $y);
        $this->pixelBlock[$x][$y] = $block;
        $this->blockLeft[$block] = min($x, $this->blockLeft[$block]);
        $this->blockRight[$block] = max($x, $this->blockRight[$block]);
        $this->blockTop[$block] = min($y, $this->blockTop[$block]);
        $this->blockBottom[$block] = max($y, $this->blockBottom[$block]);
        $this->blockSize[$block]++;

        // top
        if ($y >= 1 && !isset($this->pixelBlock[$x][$y-1]) && imagecolorat($this->im, $x, $y-1) == $pixelColor) {
            $this->foundBlock($block, $x, $y-1);
        }
        // bottom
        if ($y+1 < $this->imageHeight && !isset($this->pixelBlock[$x][$y+1]) && imagecolorat($this->im, $x, $y+1) == $pixelColor) {
            $this->foundBlock($block, $x, $y+1);
        }
        // left
        if ($x >= 1 && !isset($this->pixelBlock[$x-1][$y]) && imagecolorat($this->im, $x-1, $y) == $pixelColor) {
            $this->foundBlock($block, $x-1, $y);
        }
        // right
        if ($x+1 < $this->imageWidth && !isset($this->pixelBlock[$x+1][$y]) && imagecolorat($this->im, $x+1, $y) == $pixelColor) {
            $this->foundBlock($block, $x+1, $y);
        }
    }

    function push($value) {
        $this->stack[] = $value;
    }

    function pop() {
        $ret = $this->stack[count($this->stack)-1];
        $this->stack = array_slice($this->stack, 0, -1);
        return $ret;
    }

    function add() {
        $top = $this->pop();
        $subTop = $this->pop();
        $this->push($top + $subTop);
    }

    function substract() {
        $top = $this->pop();
        $subTop = $this->pop();
        $this->push($subTop - $top);
    }

    function multiply() {
        $top = $this->pop();
        $subTop = $this->pop();
        $this->push($top * $subTop);
    }

    function divide() {
        $top = $this->pop();
        $subTop = $this->pop();
        $this->push(floor($subTop / $top));
    }

    function mod() {
        $top = $this->pop();
        $subTop = $this->pop();
        $this->push($subTop % $top);
    }

    function not() {
        $top = $this->pop();
        $this->push($top ? 0 : 1);
    }

    function greater() {
        $top = $this->pop();
        $subTop = $this->pop();
        $this->push($top > $subTop ? 0 : 1);
    }

    function pointer() {
        $this->dp += $this->pop();
        if ($this->dp < 0) {
            $this->dp += ceil((-$this->dp)/4)*4;
        }
        $this->dp %= 4;
    }

    function ccSwitch() {
        $this->cc += $this->pop();
        if ($this->cc < 0) {
            $this->cc += ceil((-$this->cc)/2)*2;
        }
        $this->cc %= 2;
    }

    function duplicate() {
        $val = $this->pop();
        $this->push($val);
        $this->push($val);
    }

    function roll() {
        $count = $this->pop();
        $depth = $this->pop();

        for ($i=0; $i<$count; $i++) {
            $chunk = array_slice($this->stack, -abs($depth));
            $rest = array_slice($this->stack, 0, abs($depth));
            if ($depth >= 0) {
                $chunk = array_merge(array_slice($chunk, -1), array_slice($chunk, 0, -1));
            } else {
                $chunk = array_merge(array_slice($chunk, 1), array_slice($chunk, 0, 1));
            }
            $this->stack = array_merge($rest, $chunk);
        }
    }

    function in() {
        // TODO
        die('in: Not implemented');
    }

    function out($isChar) {
        $val = $this->pop();
        if ($isChar) {
            echo chr($val);
        } else {
            echo $val;
        }
        // echo "\n";
    }

    function getBlockHue($currentBlock) {
        $color = $this->blockColor[$currentBlock];
        foreach($this->colorsTable as $index => $row) {
            if (in_array($color, $row)) {
                return array_search($color, $row);
            }
        }
        return null;
    }

    function getBlockLightness($currentBlock) {
        $color = $this->blockColor[$currentBlock];
        foreach($this->colorsTable as $index => $row) {
            if (in_array($color, $row)) {
                return $index;
            }
        }
        return null;
    }

    function pushVal() {
        $this->push($this->blockSize[$this->pixelBlock[$this->x][$this->y]]);
    }

    function outChar() {
        $this->out(true);
    }
    function outNumber() {
        $this->out(false);
    }

    function nop() { }

}

new Piet('alice_shocked_mirrored.png');