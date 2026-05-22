
    const size = 15;
    const boardElement = document.querySelector("#board");
    const turnDot = document.querySelector("#turnDot");
    const turnName = document.querySelector("#turnName");
    const statusTitle = document.querySelector("#statusTitle");
    const statusText = document.querySelector("#statusText");
    const undoButton = document.querySelector("#undoButton");
    const resetButton = document.querySelector("#resetButton");
    const moveCount = document.querySelector("#moveCount");
    const timer = document.querySelector("#timer");
    const historyElement = document.querySelector("#history");

    let board = createBoard();
    let current = "black";
    let moves = [];
    let winner = null;
    let winningCells = [];
    let startedAt = null;
    let ticker = null;

    function createBoard() {
      return Array.from({ length: size }, () => Array(size).fill(null));
    }

    function buildBoard() {
      boardElement.innerHTML = "";
      const starPoints = [3, 7, 11];

      starPoints.forEach((x) => {
        starPoints.forEach((y) => {
          const star = document.createElement("span");
          star.className = "star";
          star.style.left = `${x * 100 / (size - 1)}%`;
          star.style.top = `${y * 100 / (size - 1)}%`;
          boardElement.appendChild(star);
        });
      });

      for (let y = 0; y < size; y += 1) {
        for (let x = 0; x < size; x += 1) {
          const cell = document.createElement("button");
          cell.className = "cell";
          cell.type = "button";
          cell.style.left = `${x * 100 / (size - 1)}%`;
          cell.style.top = `${y * 100 / (size - 1)}%`;
          cell.dataset.x = x;
          cell.dataset.y = y;
          cell.setAttribute("role", "gridcell");
          cell.setAttribute("aria-label", (x + 1) + "列" + (y + 1) + "行");
          cell.addEventListener("click", () => placeStone(x, y));
          boardElement.appendChild(cell);
        }
      }
    }

    function placeStone(x, y) {
      if (winner || board[y][x]) return;

      if (!startedAt) startTimer();

      board[y][x] = current;
      moves.push({ x, y, color: current });

      const line = findWinningLine(x, y, current);
      if (line.length >= 5) {
        winner = current;
        winningCells = line;
        stopTimer();
      } else if (moves.length === size * size) {
        winner = "draw";
        stopTimer();
      } else {
        current = current === "black" ? "white" : "black";
      }

      render();
    }

    function findWinningLine(x, y, color) {
      const directions = [
        [1, 0],
        [0, 1],
        [1, 1],
        [1, -1],
      ];

      for (const [dx, dy] of directions) {
        const line = [{ x, y }];
        collect(line, x, y, dx, dy, color);
        collect(line, x, y, -dx, -dy, color);

        if (line.length >= 5) {
          return line.sort((a, b) => a.y - b.y || a.x - b.x);
        }
      }

      return [];
    }

    function collect(line, x, y, dx, dy, color) {
      let nextX = x + dx;
      let nextY = y + dy;

      while (
        nextX >= 0 &&
        nextX < size &&
        nextY >= 0 &&
        nextY < size &&
        board[nextY][nextX] === color
      ) {
        line.push({ x: nextX, y: nextY });
        nextX += dx;
        nextY += dy;
      }
    }

    function undo() {
      if (!moves.length || winner) {
        if (winner) resetWinStateOnly();
        else return;
      }

      const last = moves.pop();
      board[last.y][last.x] = null;
      current = last.color;

      if (!moves.length) {
        stopTimer();
        startedAt = null;
        timer.textContent = "00:00";
      }

      render();
    }

    function resetWinStateOnly() {
      winner = null;
      winningCells = [];
    }

    function resetGame() {
      board = createBoard();
      current = "black";
      moves = [];
      winner = null;
      winningCells = [];
      startedAt = null;
      stopTimer();
      timer.textContent = "00:00";
      render();
    }

    function startTimer() {
      startedAt = Date.now();
      ticker = window.setInterval(updateTimer, 1000);
      updateTimer();
    }

    function stopTimer() {
      if (ticker) window.clearInterval(ticker);
      ticker = null;
    }

    function updateTimer() {
      if (!startedAt) return;
      const seconds = Math.floor((Date.now() - startedAt) / 1000);
      const minutePart = String(Math.floor(seconds / 60)).padStart(2, "0");
      const secondPart = String(seconds % 60).padStart(2, "0");
      timer.textContent = minutePart + ":" + secondPart;
    }

    function render() {
      document.querySelectorAll(".cell").forEach((cell) => {
        const x = Number(cell.dataset.x);
        const y = Number(cell.dataset.y);
        const color = board[y][x];
        cell.className = "cell" + (color ? " " + color : "");
        cell.disabled = Boolean(color || winner);
        cell.style.color = current === "black" ? "#111" : "#fff";
        cell.setAttribute("aria-label", (x + 1) + "列" + (y + 1) + "行" + (color ? colorName(color) : "空位"));
      });

      winningCells.forEach(({ x, y }) => {
        const cell = document.querySelector(`.cell[data-x="${x}"][data-y="${y}"]`);
        if (cell) cell.classList.add("win");
      });

      turnDot.classList.toggle("white", current === "white");
      turnName.textContent = colorName(current);
      moveCount.textContent = moves.length;
      undoButton.disabled = moves.length === 0;

      if (winner === "draw") {
        statusTitle.textContent = "平局";
        statusText.textContent = "棋盘已满，没有一方连成五子。";
      } else if (winner) {
        statusTitle.textContent = colorName(winner) + "获胜";
        statusText.textContent = "胜负已分，可以悔棋回到上一手，或直接重开。";
      } else if (moves.length) {
        const last = moves[moves.length - 1];
        statusTitle.textContent = colorName(current) + "回合";
        statusText.textContent = colorName(last.color) + "刚落在 " + (last.x + 1) + " 列 " + (last.y + 1) + " 行。";
      } else {
        statusTitle.textContent = "对局开始";
        statusText.textContent = "请黑棋落子。";
      }

      renderHistory();
    }

    function renderHistory() {
      if (!moves.length) {
        historyElement.innerHTML = '<div class="move"><span>暂无落子</span><span>--</span></div>';
        return;
      }

      historyElement.innerHTML = moves
        .slice()
        .reverse()
        .map((move, index) => {
          const step = moves.length - index;
          return '<div class="move"><span>' + step + ". " + colorName(move.color) + "</span><span>" + (move.x + 1) + "列 " + (move.y + 1) + "行</span></div>";
        })
        .join("");
    }

    function colorName(color) {
      return color === "black" ? "黑棋" : "白棋";
    }

    undoButton.addEventListener("click", undo);
    resetButton.addEventListener("click", resetGame);

    buildBoard();
    render();
  
