# WarehouseFSM (GUI version)

Swing GUI for the Group 1 warehouse FSM. It has the same four states and the same transitions as the text version:

- OpeningState
- ClientMenuState
- ClerkMenuState
- ManagerMenuState

The backend classes (StoreSystem, Client, Product, Invoice, Money, and so on) are unchanged. Only the user interface layer was replaced.

## How it works

- `Context` holds the transition matrix, the current state, the role history, and the one window every state uses.
- Each state's `run` method puts its own buttons in the window.
- Every button is an `ActionButton`, which is its own `ActionListener` and holds the action it runs. No code checks which button fired an event.
- Output goes to the text area on the right. Input (names, IDs, quantities, amounts) is requested with dialog boxes.

## Run

```
cd WarehouseFSM
javac *.java
java UserInterface
```

A window opens and asks whether to use saved warehouse data. Choose No for a fresh warehouse.

## Run in a Codespace

A Codespace has no screen, so start the virtual desktop first (tightvncserver and novnc), then:

```
export DISPLAY=:1
cd WarehouseFSM
javac *.java
java UserInterface
```

Open port 6080 in the browser to see the window.

## Files

- `UserInterface.java` starts the program.
- `Context.java` is the FSM and the window.
- `State.java`, `OpeningState.java`, `ClientMenuState.java`, `ClerkMenuState.java`, `ManagerMenuState.java` are the four states.
- `ActionButton.java` is the button class.
- Everything else is the unchanged backend.
