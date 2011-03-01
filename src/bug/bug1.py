# BUG 1
# By Andrew and Nick

from livewires import games, color
import random
from Goal import *
from Bug import *
import math

# Environmental inputs:
#   Am I bumping something?  self.bumping boolean
#   Where is the goal?       self.goal.x, self.goal.y

# Choose a next move by returning deltas for x and y
#   Move down and to the right:  (1, 1)
#   Move directly left:          (-1, 0)
#   Move up:                     (0, -1)
#   Go crazy:                    (1-2*random.random(), 1-2*random.random())

class MyBug(Bug):

	def setup(self):
		self.direction = (1, 1)
		self.lastBump = (0, 0)
		self.firstBump = (0, 0)
		self.closestBump = (0, 0)
		self.closestDist = 100000
		self.timeSinceBump = 0
		self.following = False
		self.goForClosest = False

	def choose_move(self):
		move = self.get_move()
		print "Move: " , move, " position: ", self.x, "," , self.y
		return move

	def get_move(self):
		
		#Move towards goal until you bump
		if self.following:
			if self.get_dist_to_goal() < self.closestDist:
				print "Found closer point"
				self.closestBump = (self.x, self.y)
				self.closestDist = self.get_dist_to_goal()
			if (self.x, self.y) == self.firstBump:
				print "Going for closest point"
				self.goForClosest = True
			if (self.x, self.y) == self.closestBump and self.goForClosest:
				print "Exiting following mode"
				self.following = False
				self.goForClosest = False
				self.closestDist = 10000000
		if not self.following and not self.bumping:
			return self.get_vector_to_goal()
		elif self.bumping and not self.following:
		#Go into follow mode upon bump
			self.firstBump = (self.x, self.y)
			self.following = True
			self.timeSinceBump = 0
		if self.following and not self.bumping:
			#Move until bump or timeout
			if self.timeSinceBump > 10:
				print "Timeout, turning right."
				self.turn_right()
				self.timeSinceBump = 0
			else:
				self.timeSinceBump+= 1
			return self.direction
		elif self.following and self.bumping:
			self.turn_left()
			self.timeSinceBump = 0
			return self.direction
			#Record shortest location
			#When back at start, move to shortest

	#Turns counterclockwise, to nearest right angle
	def turn_left(self):
		if self.direction == (1, 1) or self.direction == (0, 1):
			self.direction = (1, 0)
		elif self.direction == (1, -1) or self.direction == (1, 0):
			self.direction = (0, -1)
		elif self.direction == (-1, -1) or self.direction == (0, -1):
			self.direction = (-1, 0)
		else:
			self.direction = (0, 1)
	
	#Turns clockwise, to nearest right angle. Only works if you're already
	#moving at right angles.
	def turn_right(self):
		if self.direction == (0, 1):
			self.direction = (-1, 0)
		elif self.direction == (1, 0):
			self.direction = (0, 1)
		elif self.direction == (0, -1):
			self.direction = (1, 0)
		else:
			self.direction = (0, -1)

	def get_vector_to_goal(self):
		x = self.goal.x - self.x
		y = self.goal.y - self.y
		if x > 0:
			x = 1
		elif x < 0:
			x = -1
		else:
			x = 0
		
		if y > 0:
			y = 1
		if y < 0:
			y = -1
		else:
			y = 1
		return (x, y)

	def get_dist_to_goal(self):
		distX = self.goal.x - self.x
		distY = self.goal.y - self.y
		dist = math.sqrt(distX ** 2 + distY ** 2)
		return dist
