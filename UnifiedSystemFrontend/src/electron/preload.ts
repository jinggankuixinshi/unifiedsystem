import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electronAPI', {
  print: (content: string) => ipcRenderer.invoke('print', content),
  platform: process.platform
})
