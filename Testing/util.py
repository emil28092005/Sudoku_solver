# POSSIBLE USAGE KEYS
# map, keymaker_position = Utils.generate_random_map()
# proceed with map actions...


import random
from typing import (
    List,
    Tuple,
    Optional,
    Set,
)


class Utils:
    @staticmethod
    def generate_random_map() -> Tuple[List[List[str]], Optional[Tuple[int, int]]]:
        """
        Generates a random 9x9 game map with placements of 'A', 'S', and 'P'.
        'P' placements depend on the positions of 'A' and 'S'.
        
        Returns:
            A tuple containing the game map and one unoccupied square (or None if all occupied).
        """
        # Initialize a 9x9 grid with empty strings
        game_map: List[List[str]] = [[[] for _ in range(9)] for _ in range(9)]
        all_coordinates: List[Tuple[int, int]] = [(x, y) for x in range(9) for y in range(9)]

        def place_letter(
            letter: str,
            count: int,
            available: List[Tuple[int, int]],
        ) -> List[Tuple[int, int]]:
            """
            Places a specified letter on the game map a certain number of times.

            Args:
                letter: The letter to place ('A' or 'S').
                count: Number of times to place the letter.
                available: List of available coordinates.

            Returns:
                A list of coordinates where the letter was placed.
            """
            placed: List[Tuple[int, int]] = []
            for _ in range(count):
                if not available:
                    break
                x, y = random.choice(available)
                game_map[x][y] = [letter]
                placed.append((x, y))
                available.remove((x, y))
                
            return placed

        # Place "A" 0 to 3 times
        num_A: int = random.randint(0, 3)
        A_positions: List[Tuple[int, int]] = place_letter("A", num_A, all_coordinates)

        # Place "S" 0 to 1 times
        num_S: int = random.randint(0, 1)
        S_positions: List[Tuple[int, int]] = place_letter("S", num_S, all_coordinates)

        def get_moore_neighbors(x: int, y: int) -> List[Tuple[int, int]]:
            """
            Retrieves all Moore neighbors (8 surrounding cells) for a given position.

            Args:
                x: X-coordinate.
                y: Y-coordinate.

            Returns:
                A list of neighboring coordinates within bounds.
            """
            neighbors: List[Tuple[int, int]] = []
            for dx in [-1, 0, 1]:
                for dy in [-1, 0, 1]:
                    if dx == 0 and dy == 0:
                        continue
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 9 and 0 <= ny < 9:
                        neighbors.append((nx, ny))
                        
            return neighbors

        def get_von_neumann_neighbors(x: int, y: int) -> List[Tuple[int, int]]:
            """
            Retrieves all von Neumann neighbors (4 adjacent cells) for a given position.

            Args:
                x: X-coordinate.
                y: Y-coordinate.

            Returns:
                A list of neighboring coordinates within bounds.
            """
            neighbors: List[Tuple[int, int]] = []
            for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                nx, ny = x + dx, y + dy
                if 0 <= nx < 9 and 0 <= ny < 9:
                    neighbors.append((nx, ny))
                    
            return neighbors

        # Collect all possible P placement positions
        possible_P_positions: Set[Tuple[int, int]] = set()

        for x, y in A_positions:
            neighbors = get_moore_neighbors(x, y)
            possible_P_positions.update(neighbors)

        for x, y in S_positions:
            neighbors = get_von_neumann_neighbors(x, y)
            possible_P_positions.update(neighbors)

        # Remove positions already occupied by "A" or "S"
        occupied_positions: Set[Tuple[int, int]] = set(A_positions + S_positions)
        possible_P_positions = [
            pos
            for pos in possible_P_positions
            if pos not in occupied_positions and game_map[pos[0]][pos[1]] == []
        ]

        # Place "P" in all possible positions derived from "A" and "S"
        for x, y in possible_P_positions:
            game_map[x][y] = ["P"]
            if (x, y) in all_coordinates:
                all_coordinates.remove((x, y))

        # Select one unoccupied square
        chosen_unoccupied: Optional[Tuple[int, int]] = (
            random.choice(all_coordinates) if all_coordinates else None
        )

        return game_map, chosen_unoccupied

    @staticmethod
    def heuristic(pos: Tuple[int, int], goal: Tuple[int, int]) -> int:
        """
        Calculates the Manhattan distance between two positions.

        Args:
            pos: Current position as (x, y).
            goal: Goal position as (x, y).

        Returns:
            The Manhattan distance as an integer.
        """
        return abs(pos[0] - goal[0]) + abs(pos[1] - goal[1])

    @staticmethod
    def get_directions(pos: Tuple[int, int]) -> List[Tuple[int, int]]:
        """
        Returns possible moves (Up, Down, Left, Right) from the current position within bounds.

        Args:
            pos: Current position as (x, y).

        Returns:
            A list of valid adjacent positions.
        """
        moves: List[Tuple[int, int]] = [
            (pos[0] + 1, pos[1]),  # Down
            (pos[0] - 1, pos[1]),  # Up
            (pos[0], pos[1] + 1),  # Right
            (pos[0], pos[1] - 1),  # Left
        ]

        return [move for move in moves if 0 <= move[0] <= 8 and 0 <= move[1] <= 8]

    @staticmethod
    def get_directions_with_zones(
        pos: Tuple[int, int], enemies_perception_zones: Set[Tuple[int, int]]
    ) -> List[Tuple[int, int]]:
        """
        Returns possible moves from the current position excluding moves that are in danger zones.

        Args:
            pos: Current position as (x, y).
            enemies_perception_zones: A set of dangerous positions.

        Returns:
            A list of safe adjacent positions.
        """
        moves: List[Tuple[int, int]] = Utils.get_directions(pos)

        return [move for move in moves if move not in enemies_perception_zones]
