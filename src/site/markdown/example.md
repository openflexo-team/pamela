# A basic example

## A very basic model with two entities

The following code listing represents a very basic model with two entities \emph{Book} and \emph{Library}. Entity \emph{Book} defines two read-write single properties \emph{title} and \emph{ISBN} with single cardinality and with \texttt{String} type. Entity \emph{Book} also define a constructor with initial \emph{title} value. Entity \emph{Library} defines a read-write multiple properties \emph{books} referencing \emph{Book} instances. Note that this code is sufficient to execute the model, while no line of code is required (only java interface and API methods are declared here). 

[<< Approach overview](./overview.html) [Behind the scene >>](./behind_the_scene.html)

 

    
  
